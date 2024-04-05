import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun queue(rabbitAdmin: RabbitAdmin): Queue {
        return QueueBuilder
            .durable("WHISPER-QUEUE")
            .build()
    }

    @Bean
    fun directExchange(): Exchange {
        return ExchangeBuilder
            .directExchange("DIRECT-EXCHANGE-BASIC")
            .durable(true)
            .build()
    }

    @Bean
    fun firstDirectBinding(firstQueue: Queue): Binding {
        return BindingBuilder
            .bind(firstQueue)
            .to(directExchange())
            .with("TO-WHISPER-QUEUE")
            .noargs()
    }
}
