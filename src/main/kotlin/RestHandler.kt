package whisper

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.logging.Logger

@RestController
class RestHandler(
    private val rabbitTemplate: RabbitTemplate,
) {
    companion object {
        private val logger = Logger.getLogger(RestHandler::class.java.name)
        private val resultDir = File("${System.getProperty("user.home")}/results")
    }

    private val home = System.getProperty("user.home")
    private val uploadDir = File("$home/uploads/")

    @PostMapping("/upload")
        private fun upload(@RequestParam("file") file: MultipartFile?): ResponseEntity<out Map<String, Any?>> {
        logger.warning("Home is $home, uploadDir is $uploadDir")
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
            val destFile = File("$uploadDir/$token")
            file.transferTo(destFile)

            postMessage(token)

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

        return if (result != null && result.isFile) {
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

    private fun postMessage(message: String) {
        logger.info("Posting message $message")
        rabbitTemplate.convertAndSend("WHISPER-QUEUE", message)
    }

    private fun getResult(token: String): File? {
        try {
            resultDir.listFiles()?.forEach {
                if (it.nameWithoutExtension == token) return it
            }
        } catch (e: IOException) {
            logger.severe("IO err: $e")
            return null
        }
        return null
    }

}
