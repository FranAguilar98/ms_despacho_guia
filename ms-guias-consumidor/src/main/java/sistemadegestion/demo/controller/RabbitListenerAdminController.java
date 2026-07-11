package sistemadegestion.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sistemadegestion.demo.service.RabbitListenerControlService;
import sistemadegestion.demo.service.impl.ConsumirGuiaServiceImpl;

/**
 * Mismo endpoint que RabbitListenerAdminController del profesor
 * (/rabbit-listener/pausar|reanudar|status), pero protegido con Azure AD B2C
 * (rol ADMIN). "/rabbit-listener/reanudar/{id}" es el endpoint adicional que
 * exige el enunciado: enciende el consumo de la cola 1.
 */
@RestController
@RequestMapping("/rabbit-listener")
@RequiredArgsConstructor
public class RabbitListenerAdminController {

    private final RabbitListenerControlService service;

    @PostMapping("/pausar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String pausar(@PathVariable String id) {
        service.pausarListener(id);
        return "Listener pausado: " + id;
    }

    @PostMapping("/reanudar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String reanudar(@PathVariable String id) {
        service.reanudarListener(id);
        return "Listener reanudado: " + id;
    }

    @GetMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String status(@PathVariable String id) {
        return "Listener " + id + " está " + (service.isListenerRunning(id) ? "activo" : "pausado");
    }

    /** Atajo para no tener que recordar el id del listener desde Postman. */
    @GetMapping("/guias/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String statusGuias() {
        return status(ConsumirGuiaServiceImpl.LISTENER_ID);
    }

    @PostMapping("/guias/reanudar")
    @PreAuthorize("hasRole('ADMIN')")
    public String reanudarGuias() {
        return reanudar(ConsumirGuiaServiceImpl.LISTENER_ID);
    }

    @PostMapping("/guias/pausar")
    @PreAuthorize("hasRole('ADMIN')")
    public String pausarGuias() {
        return pausar(ConsumirGuiaServiceImpl.LISTENER_ID);
    }
}
