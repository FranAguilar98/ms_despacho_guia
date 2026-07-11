package sistemadegestion.demo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;
import sistemadegestion.demo.service.RabbitListenerControlService;

@Slf4j
@Service
public class RabbitListenerControlServiceImpl implements RabbitListenerControlService {

    private final RabbitListenerEndpointRegistry registry;

    public RabbitListenerControlServiceImpl(RabbitListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void pausarListener(String id) {
        MessageListenerContainer container = registry.getListenerContainer(id);
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("Listener pausado: {}", id);
        }
    }

    @Override
    public void reanudarListener(String id) {
        MessageListenerContainer container = registry.getListenerContainer(id);
        if (container != null && !container.isRunning()) {
            container.start();
            log.info("Listener reanudado: {}", id);
        }
    }

    @Override
    public boolean isListenerRunning(String id) {
        MessageListenerContainer container = registry.getListenerContainer(id);
        return container != null && container.isRunning();
    }
}
