package sistemadegestion.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistemadegestion.demo.entity.GuiaDespachoProcesada;

import java.util.Optional;

public interface GuiaDespachoProcesadaRepository extends JpaRepository<GuiaDespachoProcesada, Long> {

    boolean existsByNumeroGuia(String numeroGuia);

    Optional<GuiaDespachoProcesada> findByNumeroGuia(String numeroGuia);
}
