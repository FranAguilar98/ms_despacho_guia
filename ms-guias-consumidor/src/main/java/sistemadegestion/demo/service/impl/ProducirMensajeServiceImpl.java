package sistemadegestion.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.dto.MensajeDTO;
import sistemadegestion.demo.service.ProducirMensajeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducirMensajeServiceImpl implements ProducirMensajeService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicarMensaje(MensajeDTO mensajeDTO) {
        rabbitTemplate.convertAndSend(
                mensajeDTO.getNombreExchange(),
                mensajeDTO.getRoutingKey(),
                mensajeDTO.getContenido()
        );
        log.info("Mensaje publicado exitosamente en exchange '{}' con routing key '{}'",
                 mensajeDTO.getNombreExchange(),
                 mensajeDTO.getRoutingKey());
    }
}
