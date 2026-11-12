package mx.fes.unam.service.bloque;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.repository.BloqueRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloqueService {

    private final BloqueRepository bloqueRepository;

    public BloqueService(BloqueRepository bloqueRepository) {
        this.bloqueRepository = bloqueRepository;
    }

    public List<BloqueEntity> listar() {
        return bloqueRepository.findAll(Sort.by("nombre"));
    }
}