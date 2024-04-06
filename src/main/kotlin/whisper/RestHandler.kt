package whisper

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*
import java.util.logging.Logger


@RestController
class RestHandler(
    private val rabbitTemplate: RabbitTemplate,
) {

    private val uploadDir = File("uploads")

    companion object {
        private val logger = Logger.getLogger(RestHandler::class.java.name)
    }

    @PostMapping("/upload")
    private fun upload(@RequestParam("file") file: MultipartFile?): ResponseEntity<out Map<String, Any?>> {
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

        if (file.size > 1) {
            val response = mapOf(
                "message" to "Only one file allowed per request",
                "result" to "undefined",
                "_links" to mapOf(
                    "self" to mapOf("href" to "/upload/undefined"),
                    "upload" to mapOf("href" to "/upload")
                )
            )
            return ResponseEntity(response, HttpStatus.BAD_REQUEST)
        }

        if (!file.contentType.isNullOrEmpty()) {
            val response = mapOf(
                "message" to "Invalid file name or extension",
                "result" to file.originalFilename,
                "_links" to mapOf(
                    "self" to mapOf("href" to "/upload/${file.originalFilename}"),
                    "upload" to mapOf("href" to "/upload")
                )
            )
            return ResponseEntity(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        }

        val token = UUID.randomUUID().toString()
        val destFile = File(uploadDir, token)
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
    }
//
//    @GetMapping("/results/{token}")
//    fun results(@PathVariable token: String): ResponseEntity<Any> {
//        val result = getResult(token)
//
//        return if (result.ready) {
//            if (result.successful) {
//                val transcr = readResult(token)
//
//                ResponseEntity.ok(
//                    mapOf(
//                        "ready" to true,
//                        "successful" to true,
//                        "result" to transcr,
//                        "_links" to mapOf(
//                            "self" to mapOf("href" to "/results/$token"),
//                            "results" to mapOf("href" to "/results")
//                        )
//                    )
//                )
//            } else {
//                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(mapOf("status" to "ERROR", "error_message" to result.errorMessage))
//            }
//        } else {
//            ResponseEntity.ok(
//                mapOf(
//                    "status" to "Running",
//                    "_links" to mapOf(
//                        "self" to mapOf("href" to "/results/$token"),
//                        "results" to mapOf("href" to "/results")
//                    )
//                )
//            )
//        }
//    }

    private fun postMessage(@RequestBody message: String) {
        logger.info("Posting message $message")
        rabbitTemplate.convertAndSend("WHISPER-QUEUE", message)
    }

}
