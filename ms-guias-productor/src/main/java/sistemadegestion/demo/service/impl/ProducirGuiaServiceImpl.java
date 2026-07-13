package sistemadegestion.demo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.config.RabbitMQConfig;
import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;
import sistemadegestion.demo.exception.GuiaDespachoPublishException;
import sistemadegestion.demo.service.ProducirGuiaService;

@Slf4j
@Service
public class ProducirGuiaServiceImpl implements ProducirGuiaService {

    private final RabbitTemplate rabbitTemplate;

    public ProducirGuiaServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enviarGuia(GuiaDespachoMensajeDTO guia) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_GUIAS,
                    guia);
            log.info("Guía {} enviada a la cola {}", guia.getNumeroGuia(), RabbitMQConfig.QUEUE_GUIAS);
        } catch (AmqpException ex) {
            log.error("Fallo al publicar la guía {} en RabbitMQ: {}", guia.getNumeroGuia(), ex.getMessage());
            throw new GuiaDespachoPublishException(guia.getNumeroGuia(), ex);
        }
    }
}
