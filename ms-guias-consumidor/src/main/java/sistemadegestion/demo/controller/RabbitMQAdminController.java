package sistemadegestion.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sistemadegestion.demo.dto.BindingDTO;
import sistemadegestion.demo.service.AdminRabbitService;

@RestController
@RequestMapping("/rabbit-admin")
@RequiredArgsConstructor
public class RabbitMQAdminController {

    private final AdminRabbitService adminRabbitService;

    @PostMapping("/colas/{nombrecola}")
    public ResponseEntity<String> crearCola(@PathVariable String nombrecola) {
        adminRabbitService.crearCola(nombrecola);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + nombrecola + "' creada exitosamente");
    }

    @DeleteMapping("/colas/{nombrecola}")
    public ResponseEntity<String> eliminarCola(@PathVariable String nombrecola) {
        adminRabbitService.eliminarCola(nombrecola);
        return ResponseEntity.ok("Cola '" + nombrecola + "' eliminada exitosamente");
    }

    @PostMapping("/exchanges/{nombreexchange}")
    public ResponseEntity<String> crearExchange(@PathVariable String nombreexchange) {
        adminRabbitService.crearExchange(nombreexchange);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Exchange '" + nombreexchange + "' creado exitosamente");
    }

    @DeleteMapping("/exchanges/{nombreexchange}")
    public ResponseEntity<String> eliminarExchange(@PathVariable String nombreexchange) {
        adminRabbitService.eliminarExchange(nombreexchange);
        return ResponseEntity.ok("Exchange '" + nombreexchange + "' eliminado exitosamente");
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(@Valid @RequestBody BindingDTO bindingDTO) {
        adminRabbitService.crearBinding(bindingDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Binding creado: cola '" + bindingDTO.getNombreCola() +
                      "' con exchange '" + bindingDTO.getNombreExchange() + "'");
    }
}