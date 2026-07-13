package sistemadegestion.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sistemadegestion.demo.dto.MensajeDTO;
import sistemadegestion.demo.service.ProducirMensajeService;

@RestController
@RequestMapping("/mensaje")
@RequiredArgsConstructor
@Slf4j
public class MensajeController {

    private final ProducirMensajeService producirMensajeService;

    @PostMapping
    public ResponseEntity<String> publicarMensaje(@Valid @RequestBody MensajeDTO mensajeDTO) {
        producirMensajeService.publicarMensaje(mensajeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Mensaje publicado exitosamente en exchange '" + mensajeDTO.getNombreExchange() +
                      "' con routing key '" + mensajeDTO.getRoutingKey() + "'");
    }
}
