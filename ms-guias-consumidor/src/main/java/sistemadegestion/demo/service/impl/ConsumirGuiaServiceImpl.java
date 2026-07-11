package sistemadegestion.demo.service.impl;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.config.RabbitMQConfig;
import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;
import sistemadegestion.demo.entity.GuiaDespachoProcesada;
import sistemadegestion.demo.repository.GuiaDespachoProcesadaRepository;
import sistemadegestion.demo.service.ConsumirGuiaService;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Mismo patrón que ConsumirMensajeServiceImpl del profesor. El listener
 * (id = LISTENER_ID) no arranca solo (autoStartup=false): lo enciende/apaga
 * RabbitListenerControlServiceImpl a través de RabbitListenerEndpointRegistry,
 * expuesto por RabbitListenerAdminController — ese es el "endpoint adicional"
 * que exige el enunciado para consumir la cola 1.
 *
 * Si falla el guardado en Oracle se hace basicNack(requeue=false); como la
 * cola tiene configurado x-dead-letter-exchange (RabbitMQConfig), RabbitMQ
 * reenvía automáticamente el mensaje a cola.guias.despacho.error. No hay
 * republicación manual en Java.
 *
 * Idempotencia: numeroGuia es el identificador único del mensaje. Antes de
 * insertar, procesarGuia() revisa si ya existe una guía procesada con ese
 * número (findByNumeroGuia); si ya se procesó, no la vuelve a guardar. Esto
 * evita duplicados si RabbitMQ redelivera un mensaje ya procesado (por
 * ejemplo, si el ack se pierde por un corte de red justo después de guardar
 * en Oracle pero antes de confirmarle al broker).
 */
@Slf4j
@Service
public class ConsumirGuiaServiceImpl implements ConsumirGuiaService {

    public static final String LISTENER_ID = "listenerGuiasDespacho";

    private final GuiaDespachoProcesadaRepository repository;
    private final MessageConverter messageConverter;

    public ConsumirGuiaServiceImpl(GuiaDespachoProcesadaRepository repository, MessageConverter messageConverter) {
        this.repository = repository;
        this.messageConverter = messageConverter;
    }

    /**
     * Idempotencia: numeroGuia es el identificador único del mensaje. Si ya
     * existe una guía procesada con ese numeroGuia, no se vuelve a insertar
     * (evita duplicados si RabbitMQ redelivera el mensaje, por ejemplo porque
     * el ack se perdió por un corte de red después de guardar en Oracle).
     */
    @Override
    public GuiaDespachoProcesada procesarGuia(GuiaDespachoMensajeDTO guia) {
        var existente = repository.findByNumeroGuia(guia.getNumeroGuia());
        if (existente.isPresent()) {
            log.warn("Guía {} ya fue procesada anteriormente, se ignora (idempotencia)", guia.getNumeroGuia());
            return existente.get();
        }

        GuiaDespachoProcesada procesada = new GuiaDespachoProcesada();
        procesada.setNumeroGuia(guia.getNumeroGuia());
        procesada.setTransportista(guia.getTransportista());
        procesada.setFecha(guia.getFecha());
        procesada.setDestinatario(guia.getDestinatario());
        procesada.setDireccion(guia.getDireccion());
        procesada.setDescripcion(guia.getDescripcion());
        procesada.setPeso(guia.getPeso());
        procesada.setBultos(guia.getBultos());
        procesada.setS3Key(guia.getS3Key());
        procesada.setBucket(guia.getBucket());
        procesada.setFechaProcesado(LocalDateTime.now());
        return repository.save(procesada);
    }

    @Override
    @RabbitListener(
            id = LISTENER_ID,
            queues = RabbitMQConfig.QUEUE_GUIAS,
            containerFactory = "rabbitListenerContainerFactory",
            autoStartup = "false"
    )
    public void recibirGuiaConAckManual(Message mensaje, Channel canal) throws IOException {
        long deliveryTag = mensaje.getMessageProperties().getDeliveryTag();
        try {
            GuiaDespachoMensajeDTO guia = (GuiaDespachoMensajeDTO) messageConverter.fromMessage(mensaje);
            log.info("Mensaje recibido de cola 1: {}", guia.getNumeroGuia());

            procesarGuia(guia);

            canal.basicAck(deliveryTag, false);
            log.info("Guía {} guardada en Oracle Cloud - Ack OK", guia.getNumeroGuia());
        } catch (Exception e) {
            log.error("Error procesando mensaje de cola 1, se envía a la cola de error: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
        }
    }
}
