package sistemadegestion.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guia_despacho_procesada")
public class GuiaDespachoProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroGuia;

    private String transportista;
    private String fecha;
    private String destinatario;
    private String direccion;
    private String descripcion;
    private double peso;
    private int bultos;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private LocalDateTime fechaProcesado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }

    public String getTransportista() { return transportista; }
    public void setTransportista(String transportista) { this.transportista = transportista; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public int getBultos() { return bultos; }
    public void setBultos(int bultos) { this.bultos = bultos; }

    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public LocalDateTime getFechaProcesado() { return fechaProcesado; }
    public void setFechaProcesado(LocalDateTime fechaProcesado) { this.fechaProcesado = fechaProcesado; }
}
