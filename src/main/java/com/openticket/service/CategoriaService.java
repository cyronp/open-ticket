package com.openticket.service;

import com.openticket.dto.CategoriaRequest;
import com.openticket.dto.CategoriaResponse;
import com.openticket.entity.Categoria;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest dto) {
        if (repository.existsByNomeIgnoreCase(dto.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com o nome " + dto.nome());
        }
        Categoria salva = repository.save(new Categoria(dto.nome(), dto.descricao()));
        return CategoriaResponse.de(salva);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(buscarEntity(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest dto) {
        Categoria categoria = buscarEntity(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dto.nome(), id)) {
            throw new RegraNegocioException("Já existe uma categoria com o nome " + dto.nome());
        }

        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Categoria buscarEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada!"));
    }
}
