package sistemadegestion.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sistemadegestion.demo.config.RabbitMQConfig;

import java.util.Map;

@RestController
@RequestMapping("/api/consumo/guias")
@RequiredArgsConstructor
public class GuiaColaEstadoController {

    private final RabbitAdmin rabbitAdmin;

    @GetMapping("/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> estadoColas() {
        QueueInformation guias = rabbitAdmin.getQueueInfo(RabbitMQConfig.QUEUE_GUIAS);
        QueueInformation guiasError = rabbitAdmin.getQueueInfo(RabbitMQConfig.QUEUE_GUIAS_ERROR);
        return ResponseEntity.ok(Map.of(
                RabbitMQConfig.QUEUE_GUIAS, guias != null ? guias.getMessageCount() : 0,
                RabbitMQConfig.QUEUE_GUIAS_ERROR, guiasError != null ? guiasError.getMessageCount() : 0
        ));
    }
}
