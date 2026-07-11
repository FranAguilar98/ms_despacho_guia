package sistemadegestion.demo.exception;

public class GuiaDespachoPublishException extends RuntimeException {

    public GuiaDespachoPublishException(String numeroGuia, Throwable causa) {
        super("No fue posible encolar la guía " + numeroGuia
                + " en RabbitMQ. Verifique que el broker esté disponible.", causa);
    }
}
