package sistemadegestion.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;
import sistemadegestion.demo.dto.MensajeDTO;
import sistemadegestion.demo.service.ProducirMensajeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducirMensajeServiceImpl implements ProducirMensajeService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publicarMensaje(MensajeDTO mensajeDTO) {
        Object cuerpoAEnviar = mensajeDTO.getContenido();

        boolean esMensajeDeError = mensajeDTO.getContenido() != null
                && mensajeDTO.getContenido().trim().equalsIgnoreCase("error");

        if (esMensajeDeError) {
            log.info("Contenido 'error' recibido: se publicará a propósito como texto plano " +
                     "para forzar el fallo de procesamiento");
        } else {
            try {
                GuiaDespachoMensajeDTO guia = objectMapper.readValue(
                        mensajeDTO.getContenido(), GuiaDespachoMensajeDTO.class);
                cuerpoAEnviar = guia;
                log.info("Contenido interpretado como GuiaDespachoMensajeDTO válida, numeroGuia={}",
                         guia.getNumeroGuia());
            } catch (Exception e) {
                log.info("Contenido no es un JSON de guía válido, se publicará como texto plano");
            }
        }

        rabbitTemplate.convertAndSend(
                mensajeDTO.getNombreExchange(),
                mensajeDTO.getRoutingKey(),
                cuerpoAEnviar
        );
        log.info("Mensaje publicado exitosamente en exchange '{}' con routing key '{}'",
                 mensajeDTO.getNombreExchange(),
                 mensajeDTO.getRoutingKey());
    }
}