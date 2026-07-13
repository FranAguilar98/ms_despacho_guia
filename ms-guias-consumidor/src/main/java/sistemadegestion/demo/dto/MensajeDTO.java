package sistemadegestion.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String nombreExchange;

    @NotBlank(message = "La routing key es obligatoria")
    private String routingKey;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    private String contenido;
}
