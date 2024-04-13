package whisper.application

import application.util.createPath
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files

const val UPLOAD_DIR = "/tmp/bastter-whisper/upload"
const val RESULT_DIR = "/tmp/bastter-whisper/result"

@SpringBootApplication
class RestService

fun main(args: Array<String>) {
    createPath(UPLOAD_DIR)?.let { Files.createDirectories(it) }
    createPath(RESULT_DIR)?.let { Files.createDirectories(it) }
    runApplication<RestService>(*args)
}
