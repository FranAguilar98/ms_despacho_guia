package sistemadegestion.demo.service;

import sistemadegestion.demo.dto.GuiaDespachoMensajeDTO;

public interface ProducirGuiaService {

    void enviarGuia(GuiaDespachoMensajeDTO guia);
}
