package whisper_rest_producer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestService

fun main(args: Array<String>) {
    runApplication<RestService>(*args)
}
