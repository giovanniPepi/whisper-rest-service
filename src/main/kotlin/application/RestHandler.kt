package application

import application.util.getPath
import application.util.getResult
import application.util.logger
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import whisper.application.UPLOAD_DIR
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import kotlin.io.path.inputStream


@RestController
class RestHandler(
    private val rabbitTemplate: RabbitTemplate,
) {
    @PostMapping("/upload")
    private fun upload(@RequestParam("file") file: MultipartFile?): ResponseEntity<out Map<String, Any?>> {
        logger.warning("File received: $file, type is ${file?.contentType}")

        if (file == null || file.isEmpty) {
            val response = mapOf(
                "message" to "File not found in request",
                "result" to "undefined",
                "_links" to mapOf(
                    "self" to mapOf("href" to "/upload/undefined"),
                    "upload" to mapOf("href" to "/upload")
                )
            )
            return ResponseEntity(response, HttpStatus.BAD_REQUEST)
        }

        try {
            val token = UUID.randomUUID().toString()
            getPath("$UPLOAD_DIR/$token")?.apply {
                resolve(file.originalFilename!!)
                Files.copy(file.inputStream, this)
            }

            postMessageToQueue(token)

            val response = mapOf(
                "message" to "Processing file.",
                "_links" to mapOf(
                    "self" to mapOf("href" to "/results/$token"),
                    "results" to mapOf("href" to "/results")
                )

            )
            return ResponseEntity(response, HttpStatus.ACCEPTED)
        } catch (e: IOException) {
            logger.severe("Io error: $e")
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/results/{token}")
    fun results(@PathVariable token: String): ResponseEntity<Any> {
        val result = getResult(token)

        return if (result != null) {
            val transcription = result.inputStream().bufferedReader(StandardCharsets.UTF_8).use {
                it.readText()
            }

            ResponseEntity.ok(
                mapOf(
                    "ready" to true,
                    "successful" to true,
                    "result" to transcription,
                    "_links" to mapOf(
                        "self" to mapOf("href" to "/results/$token"),
                        "results" to mapOf("href" to "/results")
                    )
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("status" to "ERROR", "error_message" to "Transcription result not found"))
        }
    }

    private fun postMessageToQueue(message: String) {
        logger.info("Posting message $message")
        rabbitTemplate.convertAndSend("WHISPER-QUEUE", message)
    }
}
