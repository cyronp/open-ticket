package com.openticket.service;

import com.openticket.dto.ChamadoAtribuirRequest;
import com.openticket.dto.ChamadoAtualizacaoRequest;
import com.openticket.dto.ChamadoDetalheResponse;
import com.openticket.dto.ChamadoRequest;
import com.openticket.dto.ChamadoResponse;
import com.openticket.dto.ChamadoStatusRequest;
import com.openticket.dto.ComentarioRequest;
import com.openticket.dto.ComentarioResponse;
import com.openticket.entity.*;
import com.openticket.exception.ChamadoFechadoException;
import com.openticket.exception.OrdenacaoInvalidaException;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.repository.CategoriaRepository;
import com.openticket.repository.ChamadoRepository;
import com.openticket.repository.ClienteRepository;
import com.openticket.repository.ComentarioRepository;
import com.openticket.repository.TecnicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChamadoService {
    private static final Set<String> CAMPOS_ORDENACAO = new LinkedHashSet<>(List.of(
            "id", "titulo", "dataAbertura", "dataFechamento", "prioridade", "status", "cliente.nome", "categoria.nome"
    ));

    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ComentarioRepository comentarioRepository;

    public ChamadoService(
            ChamadoRepository repository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository,
            ComentarioRepository comentarioRepository
    ) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.comentarioRepository = comentarioRepository;
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
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable) {
        validarOrdenacao(pageable.getSort());
        return repository.filtrar(status, prioridade, clienteId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, StatusChamado status, Prioridade prioridade, Pageable pageable) {
        buscarCliente(clienteId);
        return listar(status, prioridade, clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscar(Long id) {
        return ChamadoDetalheResponse.de(buscarEntity(id));
    }

    @Transactional
    public ComentarioResponse comentar(Long id, ComentarioRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

        Comentario comentario = comentarioRepository.save(new Comentario(dto.texto(), dto.autor(), chamado));
        return ComentarioResponse.de(comentario);
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoAtualizacaoRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

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
        Chamado chamado = buscarEntityAberto(id);

        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, ChamadoStatusRequest dto) {
        Chamado chamado = buscarEntityAberto(id);

        atualizarStatus(chamado, dto.status());
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    // Chamado fechado é estado final, então só chega aqui chamado ainda não fechado
    private void atualizarStatus(Chamado chamado, StatusChamado novoStatus) {
        if (novoStatus == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        chamado.setStatus(novoStatus);
    }

    // O sort vai direto para o JPQL, então só aceita campos conhecidos.
    // tecnico.nome fica de fora porque o join implícito esconderia chamados sem técnico
    private void validarOrdenacao(Sort sort) {
        for (Sort.Order ordem : sort) {
            if (!CAMPOS_ORDENACAO.contains(ordem.getProperty())) {
                throw new OrdenacaoInvalidaException(ordem.getProperty(), CAMPOS_ORDENACAO);
            }
        }
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado " + id + " não encontrado!"));
    }

    private Chamado buscarEntityAberto(Long id) {
        Chamado chamado = buscarEntity(id);
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new ChamadoFechadoException(id);
        }
        return chamado;
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
