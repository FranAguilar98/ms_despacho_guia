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

    /**
     * Publica la guía en cola.guias.despacho (routing key guias.despacho).
     * Si el broker no está disponible, no se guarda un estado a medias:
     * se lanza GuiaDespachoPublishException para que el controller devuelva
     * un error claro en vez de un 500 genérico con stacktrace de AMQP.
     *
     * Si el mensaje SÍ llega a la cola pero el CONSUMIDOR falla al procesarlo,
     * ese caso lo cubre el Dead Letter Exchange configurado en la cola
     * (ver RabbitMQConfig): RabbitMQ reenvía automáticamente a la cola de
     * error, no hace falta manejarlo aquí.
     */
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
