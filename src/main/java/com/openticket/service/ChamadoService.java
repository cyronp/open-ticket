package com.openticket.service;

import com.openticket.dto.ChamadoAtribuirRequest;
import com.openticket.dto.ChamadoAtualizacaoRequest;
import com.openticket.dto.ChamadoRequest;
import com.openticket.dto.ChamadoResponse;
import com.openticket.entity.*;
import com.openticket.exception.ChamadoFechadoException;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.repository.CategoriaRepository;
import com.openticket.repository.ChamadoRepository;
import com.openticket.repository.ClienteRepository;
import com.openticket.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChamadoService {
    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(
            ChamadoRepository repository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Chamado chamado = new Chamado(
                dto.titulo(),
                dto.descricao(),
                dto.prioridade(),
                buscarCliente(dto.clienteId()),
                buscarCategoria(dto.categoriaId()),
                buscarTecnico(dto.tecnicoId())
        );
        return ChamadoResponse.de(repository.save(chamado));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar() {
        return repository.findAll().stream().map(ChamadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id) {
        return ChamadoResponse.de(buscarEntity(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoAtualizacaoRequest dto) {
        Chamado chamado = buscarEntity(id);

        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        atualizarStatus(chamado, dto.status());
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse atribuir(Long id, ChamadoAtribuirRequest dto) {
        Chamado chamado = buscarEntity(id);

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new ChamadoFechadoException(id);
        }

        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));

        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    // Ao fechar registra a data de fechamento; ao reabrir, limpa
    private void atualizarStatus(Chamado chamado, StatusChamado novoStatus) {
        if (novoStatus == StatusChamado.FECHADO && chamado.getStatus() != StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        } else if (novoStatus != StatusChamado.FECHADO) {
            chamado.setDataFechamento(null);
        }
        chamado.setStatus(novoStatus);
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado " + id + " não encontrado!"));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado!"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada!"));
    }

    private Tecnico buscarTecnico(Long id) {
        if (id == null) {
            return null;
        }
        return tecnicoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Técnico " + id + " não encontrado!"));
    }
}
