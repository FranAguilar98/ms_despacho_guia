package sistemadegestion.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindingDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String nombreCola;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String nombreExchange;

    @NotBlank(message = "La routing key es obligatoria")
    private String routingKey;
}
