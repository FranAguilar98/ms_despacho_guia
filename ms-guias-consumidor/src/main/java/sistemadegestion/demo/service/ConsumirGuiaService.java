package sistemadegestion.demo.service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;
import sistemadegestion.demo.entity.GuiaDespachoProcesada;

import java.io.IOException;

public interface ConsumirGuiaService {

    /** Persiste una guía ya deserializada en Oracle Cloud. */
    GuiaDespachoProcesada procesarGuia(GuiaDespachoMensajeDTO guia);

    /**
     * Método que escucha la cola 1 con ack manual (mismo patrón que
     * recibirMensajeConAckManual del profesor): ack si se guarda bien,
     * nack (sin requeue) si falla, y el Dead Letter Exchange de la cola
     * reenvía automáticamente a la cola de error.
     */
    void recibirGuiaConAckManual(Message mensaje, Channel canal) throws IOException;
}
