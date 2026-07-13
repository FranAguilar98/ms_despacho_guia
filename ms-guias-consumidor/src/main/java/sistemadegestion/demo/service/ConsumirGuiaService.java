package sistemadegestion.demo.service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;
import sistemadegestion.demo.entity.GuiaDespachoProcesada;

import java.io.IOException;

public interface ConsumirGuiaService {

    GuiaDespachoProcesada procesarGuia(GuiaDespachoMensajeDTO guia);

    void recibirGuiaConAckManual(Message mensaje, Channel canal) throws IOException;
}
