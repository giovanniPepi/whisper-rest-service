package application

import RESULT_DIR
import UPLOAD_DIR
import createPath
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files


@SpringBootApplication
class RestService

fun main(args: Array<String>) {
    createPath(UPLOAD_DIR)?.let { Files.createDirectories(it) }
    createPath(RESULT_DIR)?.let { Files.createDirectories(it) }
    runApplication<RestService>(*args)
}
