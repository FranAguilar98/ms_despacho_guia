package sistemadegestion.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.dto.BindingDTO;
import sistemadegestion.demo.service.AdminRabbitService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRabbitServiceImpl implements AdminRabbitService {

    private final RabbitAdmin rabbitAdmin;

    @Override
    public void crearCola(String nombreCola) {
        Queue queue = QueueBuilder.durable(nombreCola).build();
        rabbitAdmin.declareQueue(queue);
        log.info("Cola '{}' creada exitosamente", nombreCola);
    }

    @Override
    public void eliminarCola(String nombreCola) {
        rabbitAdmin.deleteQueue(nombreCola);
        log.info("Cola '{}' eliminada exitosamente", nombreCola);
    }

    @Override
    public void crearExchange(String nombreExchange) {
        DirectExchange exchange = new DirectExchange(nombreExchange);
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange '{}' creado exitosamente", nombreExchange);
    }

    @Override
    public void eliminarExchange(String nombreExchange) {
        rabbitAdmin.deleteExchange(nombreExchange);
        log.info("Exchange '{}' eliminado exitosamente", nombreExchange);
    }

    @Override
    public void crearBinding(BindingDTO bindingDTO) {
        Queue queue = QueueBuilder.durable(bindingDTO.getNombreCola()).build();
        DirectExchange exchange = new DirectExchange(bindingDTO.getNombreExchange());
        
        var binding = BindingBuilder.bind(queue)
                .to(exchange)
                .with(bindingDTO.getRoutingKey());
        
        rabbitAdmin.declareBinding(binding);
        log.info("Binding creado: cola '{}' - exchange '{}' - routing key '{}'", 
                 bindingDTO.getNombreCola(), 
                 bindingDTO.getNombreExchange(),
                 bindingDTO.getRoutingKey());
    }
}
