import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class WhisperQueueConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["WHISPER-QUEUE"])
    fun receivemessageFromQueue(message: Message) {
        val bodyAsString = message.body?.let { String(it) }
        log.info("body from message: $bodyAsString")
    }
}