package application.rest

import UPLOAD_DIR
import WhisperUtils
import WhisperUtils.Companion.logger
import application.processbuilder.VideoProcessBuilder
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import kotlin.io.path.inputStream


@RestController
class RestHandler(
    private val rabbitTemplate: RabbitTemplate,
) {
    companion object {
        val whisperUtils = WhisperUtils()
        private val readyJobs = mutableListOf<String>()

    }

    @PostMapping("/upload")
    private fun upload(
        @RequestParam("file") file: MultipartFile?,
        @RequestParam("link") link: String?,
    ): ResponseEntity<out Map<String, Any?>> {
        val token = UUID.randomUUID().toString()
        logger.warning("Upload request: Token $token, file $file, type ${file?.contentType}, link $link")

        // handle link
        if (!link.isNullOrEmpty()) {
            val videoStatus = VideoProcessBuilder().runYtDlp(link = link, token = token)
            if (videoStatus != 0) {
                val response = mapOf(
                    "message" to "Error processing media from link", "result" to "undefined", "_links" to mapOf(
                        "self" to mapOf("href" to "/upload/undefined"), "upload" to mapOf("href" to "/upload")
                    )
                )
                return ResponseEntity(response, HttpStatus.BAD_REQUEST)
            }
        } else {
            // handle null file
            if (file == null || file.isEmpty) {
                val response = mapOf(
                    "message" to "File not found in request", "result" to "undefined", "_links" to mapOf(
                        "self" to mapOf("href" to "/upload/undefined"), "upload" to mapOf("href" to "/upload")
                    )
                )
                return ResponseEntity(response, HttpStatus.BAD_REQUEST)
            } else {
                try {
                    whisperUtils.getPath("$UPLOAD_DIR/$token")?.apply {
                        resolve(file.originalFilename!!)
                        Files.copy(file.inputStream, this)
                    }

                } catch (e: IOException) {
                    logger.severe("Io error: $e")
                    return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }

        postMessageToQueue(token)
        val response = mapOf(
            "message" to "Processing $token", "_links" to mapOf(
                "self" to mapOf("href" to "/results/$token"), "results" to mapOf("href" to "/results")
            )
        )
        return ResponseEntity(response, HttpStatus.ACCEPTED)

    }

    @GetMapping("/results/{token}")
    fun result(@PathVariable token: String): ResponseEntity<Any> {
        val result = whisperUtils.getFile(token)
        removeReadyJob(token)
        return if (result != null) {
            val transcription = result.inputStream().bufferedReader(StandardCharsets.UTF_8).use {
                it.readText()
            }

            ResponseEntity.ok(
                mapOf(
                    "ready" to true, "result" to transcription, "_links" to mapOf(
                        "self" to mapOf("href" to "/results/$token"), "results" to mapOf("href" to "/results")
                    )
                )
            )
        } else {
            ResponseEntity.ok(
                mapOf(
                    "ready" to false, "result" to "", "_links" to mapOf(
                        "self" to mapOf("href" to "/results/$token"), "results" to mapOf("href" to "/results")
                    )
                )
            )
        }

    }

    @GetMapping("/results")
    fun results(): ResponseEntity<Map<String, Any>> {
        val readyResults = getReadyTokens()

        val resultsList = readyResults.map { result ->
            mapOf("href" to "/results/$result")
        }

        return ResponseEntity.ok(
            mapOf(
                "results" to resultsList, "_links" to mapOf(
                    "self" to mapOf("href" to "/results/"), "results" to mapOf("href" to "/results")
                )
            )
        )
    }

    @DeleteMapping("/results/gc")
    fun resultGc(): ResponseEntity<Void> {
        whisperUtils.deleteAllResults()

        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/upload/gc")
    fun uploadGc(): ResponseEntity<Void> {
        whisperUtils.deleteAllUploads()

        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }


    private fun postMessageToQueue(message: String) {
        logger.info("Posting message $message")
        rabbitTemplate.convertAndSend("WHISPER-QUEUE", message)
    }

    @RabbitListener(queues = ["WHISPER-READY-QUEUE"])
    private fun markTokenAsReady(message: Message) {
        message.body?.let {
            val token = String(it)
            readyJobs.add(token)
            logger.info { "REST handler received ready token: $token" }
        }
    }

    fun getReadyTokens(): List<String> {
        return readyJobs
    }

    fun removeReadyJob(token: String) {
        readyJobs.remove(token)
    }
}
