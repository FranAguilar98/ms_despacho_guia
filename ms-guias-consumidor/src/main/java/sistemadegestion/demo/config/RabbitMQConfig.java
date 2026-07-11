package sistemadegestion.demo.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "exchange.guias.despacho";
    public static final String ERROR_EXCHANGE = "exchange.guias.despacho.error";

    public static final String QUEUE_GUIAS = "cola.guias.despacho";
    public static final String ROUTING_KEY_GUIAS = "guias.despacho";

    public static final String QUEUE_GUIAS_ERROR = "cola.guias.despacho.error";
    public static final String ROUTING_KEY_GUIAS_ERROR = "guias.despacho.error";


    @Bean
    public Queue queueGuias() {
        return QueueBuilder.durable(QUEUE_GUIAS)
                .withArgument("x-dead-letter-exchange", ERROR_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_GUIAS_ERROR)
                .build();
    }

    @Bean
    public Queue queueGuiasError() {
        return QueueBuilder.durable(QUEUE_GUIAS_ERROR).build();
    }

    @Bean
    public DirectExchange exchangeGuias() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange errorExchange() {
        return new DirectExchange(ERROR_EXCHANGE);
    }

    @Bean
    public Binding bindingGuias(Queue queueGuias, DirectExchange exchangeGuias) {
        return BindingBuilder.bind(queueGuias).to(exchangeGuias).with(ROUTING_KEY_GUIAS);
    }

    @Bean
    public Binding bindingGuiasError(Queue queueGuiasError, DirectExchange errorExchange) {
        return BindingBuilder.bind(queueGuiasError).to(errorExchange).with(ROUTING_KEY_GUIAS_ERROR);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
