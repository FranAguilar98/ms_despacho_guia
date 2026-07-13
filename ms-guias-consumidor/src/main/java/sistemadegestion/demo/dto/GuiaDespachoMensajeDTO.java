package sistemadegestion.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GuiaDespachoMensajeDTO implements Serializable {

    @NotBlank(message = "El número de guía es obligatorio")
    private String numeroGuia;

    @NotBlank(message = "El transportista es obligatorio")
    private String transportista;

    @NotBlank(message = "La fecha es obligatoria (formato yyyy-MM-dd)")
    private String fecha;

    private String destinatario;
    private String direccion;
    private String descripcion;

    @PositiveOrZero(message = "El peso no puede ser negativo")
    private double peso;

    @PositiveOrZero(message = "La cantidad de bultos no puede ser negativa")
    private int bultos;

    @NotBlank(message = "La key de S3 es obligatoria")
    private String s3Key;

    @NotBlank(message = "El bucket es obligatorio")
    private String bucket;

    private LocalDateTime timestampEnvio;

    public GuiaDespachoMensajeDTO(String numeroGuia, String transportista, String fecha, String destinatario,
                                   String direccion, String descripcion, double peso, int bultos,
                                   String s3Key, String bucket) {
        this.numeroGuia = numeroGuia;
        this.transportista = transportista;
        this.fecha = fecha;
        this.destinatario = destinatario;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.peso = peso;
        this.bultos = bultos;
        this.s3Key = s3Key;
        this.bucket = bucket;
        this.timestampEnvio = LocalDateTime.now();
    }
}
