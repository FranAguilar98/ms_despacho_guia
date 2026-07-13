package sistemadegestion.demo.service;

import sistemadegestion.demo.dto.MensajeDTO;

public interface ProducirMensajeService {

    /**
     * Publica un mensaje genérico en RabbitMQ
     * 
     * @param mensajeDTO DTO con nombreExchange, routingKey y contenido
     */
    void publicarMensaje(MensajeDTO mensajeDTO);
}
