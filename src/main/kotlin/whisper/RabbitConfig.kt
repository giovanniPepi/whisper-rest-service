package whisper

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        return RabbitTemplate(connectionFactory)
    }

    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    fun queue(rabbitAdmin: RabbitAdmin): Queue {
        return QueueBuilder
            .durable("WHISPER-QUEUE")
            .build()
    }

    @Bean
    fun directExchange(): Exchange {
        return ExchangeBuilder
            .directExchange("DIRECT-EXCHANGE")
            .durable(true)
            .build()
    }

    @Bean
    fun directBinding(queue: Queue): Binding {
        return BindingBuilder
            .bind(queue)
            .to(directExchange())
            .with("TO-WHISPER-QUEUE")
            .noargs()
    }
}
