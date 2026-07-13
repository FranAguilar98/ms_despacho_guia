package sistemadegestion.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sistemadegestion.demo.service.RabbitListenerControlService;
import sistemadegestion.demo.service.impl.ConsumirGuiaServiceImpl;

@RestController
@RequestMapping("/rabbit-listener")
@RequiredArgsConstructor
public class RabbitListenerAdminController {

    private final RabbitListenerControlService service;

    @PostMapping("/pausar/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String pausar(@PathVariable String id) {
        service.pausarListener(id);
        return "Listener pausado: " + id;
    }

    @PostMapping("/reanudar/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String reanudar(@PathVariable String id) {
        service.reanudarListener(id);
        return "Listener reanudado: " + id;
    }

    @GetMapping("/status/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String status(@PathVariable String id) {
        return "Listener " + id + " está " + (service.isListenerRunning(id) ? "activo" : "pausado");
    }

    /** Atajo para no tener que recordar el id del listener desde Postman. */
    @GetMapping("/guias/status")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String statusGuias() {
        return status(ConsumirGuiaServiceImpl.LISTENER_ID);
    }

    @PostMapping("/guias/reanudar")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String reanudarGuias() {
        return reanudar(ConsumirGuiaServiceImpl.LISTENER_ID);
    }

    @PostMapping("/guias/pausar")
    // @PreAuthorize("hasRole('ADMIN')") // Comentado: Access Token (Client Credentials) 
    public String pausarGuias() {
        return pausar(ConsumirGuiaServiceImpl.LISTENER_ID);
    }
}
