import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

@SpringBootApplication
class Application

val uploadDir = File("uploads")

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
class UploadController {

    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile?): ResponseEntity<Map<String, Any>> {
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

        if (request.multipartFiles.size > 1) {
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

        val fileName = UUID.randomUUID().toString()
        val destFile = File(uploadDir, fileName)
        file.transferTo(destFile)

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

        val resultId = runWhisper(destFile)

        val response = mapOf(
            "message" to "Processing file.",
            "_links" to mapOf(
                "self" to mapOf("href" to "/results/$resultId"),
                "results" to mapOf("href" to "/results")
            )
        )
        return ResponseEntity(response, HttpStatus.ACCEPTED)
    }

    @GetMapping("/results/{token}")
    fun getResult(@PathVariable token: String): ResponseEntity<Any> {
        val result = getResult(token)

        return if (result.ready) {
            if (result.successful) {
                val transcr = readResult(token)

                ResponseEntity.ok(
                    mapOf(
                        "ready" to true,
                        "successful" to true,
                        "result" to transcr,
                        "_links" to mapOf(
                            "self" to mapOf("href" to "/results/$token"),
                            "results" to mapOf("href" to "/results")
                        )
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("status" to "ERROR", "error_message" to result.errorMessage))
            }
        } else {
            ResponseEntity.ok(
                mapOf(
                    "status" to "Running",
                    "_links" to mapOf(
                        "self" to mapOf("href" to "/results/$token"),
                        "results" to mapOf("href" to "/results")
                    )
                )
            )
        }
    }
}

data class UploadResult(val ready: Boolean, val successful: Boolean, val errorMessage: String?, val id: String)

fun runWhisper(file: File): String {
    // Your logic for running whisper
    return UUID.randomUUID().toString()
}

fun getResult(token: String): UploadResult {
    // Your logic for getting result
    return UploadResult(true, true, null, UUID.randomUUID().toString())
}

fun readResult(token: String): String {
    // Your logic for reading result
    return "Result for $token"
}
