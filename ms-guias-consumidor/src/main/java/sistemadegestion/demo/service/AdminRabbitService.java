package sistemadegestion.demo.service;

import sistemadegestion.demo.dto.BindingDTO;

public interface AdminRabbitService {

    /**
     * Crea una cola de RabbitMQ
     * 
     * @param nombreCola nombre de la cola a crear
     */
    void crearCola(String nombreCola);

    /**
     * Elimina una cola de RabbitMQ
     * 
     * @param nombreCola nombre de la cola a eliminar
     */
    void eliminarCola(String nombreCola);

    /**
     * Crea un exchange de tipo DirectExchange
     * 
     * @param nombreExchange nombre del exchange a crear
     */
    void crearExchange(String nombreExchange);

    /**
     * Elimina un exchange de RabbitMQ
     * 
     * @param nombreExchange nombre del exchange a eliminar
     */
    void eliminarExchange(String nombreExchange);

    /**
     * Crea un binding entre una cola y un exchange
     * 
     * @param bindingDTO DTO con nombreCola y nombreExchange
     */
    void crearBinding(BindingDTO bindingDTO);
}
