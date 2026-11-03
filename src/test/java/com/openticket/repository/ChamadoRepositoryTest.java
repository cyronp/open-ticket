package com.openticket.repository;

import com.openticket.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ChamadoRepositoryTest {
    private static final Pageable PAGINA = PageRequest.of(0, 10, Sort.by("id"));

    @Autowired
    private ChamadoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    private Cliente ana;
    private Cliente bruno;
    private Categoria hardware;
    private Tecnico joao;

    @BeforeEach
    void setUp() {
        ana = clienteRepository.save(new Cliente("Ana Souza", "ana@x.com", null));
        bruno = clienteRepository.save(new Cliente("Bruno Lima", "bruno@x.com", null));
        hardware = categoriaRepository.save(new Categoria("Hardware", null));
        joao = tecnicoRepository.save(new Tecnico("João", "joao@x.com", Perfil.TECNICO));
    }

    @Test
    void salvarDefineStatusAbertoEDataAbertura() {
        Chamado salvo = repository.save(chamado("Mouse quebrado", Prioridade.BAIXA, ana));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusChamado.ABERTO);
        assertThat(salvo.getDataAbertura()).isNotNull();
        assertThat(salvo.getDataFechamento()).isNull();
    }

    @Test
    void salvarSemClienteFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(chamado("Mouse quebrado", Prioridade.BAIXA, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarSemCategoriaFalha() {
        Chamado semCategoria = new Chamado("Mouse quebrado", "Não clica", Prioridade.BAIXA, ana, null, null);

        assertThatThrownBy(() -> repository.saveAndFlush(semCategoria))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void buscarInexistenteRetornaVazio() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void filtrarSemFiltrosRetornaTodos() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PAGINA);

        assertThat(pagina.getTotalElements()).isEqualTo(4);
    }

    @Test
    void filtrarPorStatus() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.FECHADO, null, null, PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Servidor fora");
    }

    @Test
    void filtrarPorPrioridade() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, Prioridade.ALTA, null, PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("ERP lento", "Acesso VPN");
    }

    @Test
    void filtrarPorCliente() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, bruno.getId(), PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Acesso VPN");
    }

    @Test
    void filtrarCombinado() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.ABERTO, Prioridade.ALTA, ana.getId(), PAGINA);

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("ERP lento");
    }

    @Test
    void filtrarSemResultado() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(StatusChamado.AGUARDANDO_CLIENTE, null, null, PAGINA);

        assertThat(pagina.getContent()).isEmpty();
        assertThat(pagina.getTotalElements()).isZero();
    }

    @Test
    void filtrarPaginaEOrdena() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(1, 2, Sort.by("titulo")));

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Mouse quebrado", "Servidor fora");
        assertThat(pagina.getTotalElements()).isEqualTo(4);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void filtrarOrdenaPorCampoDoCliente() {
        criarMassa();

        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "cliente.nome")));

        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Acesso VPN");
    }

    @Test
    void existsByClienteId() {
        repository.save(chamado("Mouse quebrado", Prioridade.BAIXA, ana));

        assertThat(repository.existsByClienteId(ana.getId())).isTrue();
        assertThat(repository.existsByClienteId(bruno.getId())).isFalse();
    }

    @Test
    void existsByCategoriaId() {
        Categoria software = categoriaRepository.save(new Categoria("Software", null));
        repository.save(chamado("Mouse quebrado", Prioridade.BAIXA, ana));

        assertThat(repository.existsByCategoriaId(hardware.getId())).isTrue();
        assertThat(repository.existsByCategoriaId(software.getId())).isFalse();
    }

    @Test
    void existsByTecnicoId() {
        Tecnico marta = tecnicoRepository.save(new Tecnico("Marta", "marta@x.com", Perfil.ADMIN));
        Chamado chamado = chamado("Mouse quebrado", Prioridade.BAIXA, ana);
        chamado.setTecnico(joao);
        repository.save(chamado);

        assertThat(repository.existsByTecnicoId(joao.getId())).isTrue();
        assertThat(repository.existsByTecnicoId(marta.getId())).isFalse();
    }

    private void criarMassa() {
        repository.save(chamado("Mouse quebrado", Prioridade.BAIXA, ana));
        repository.save(chamado("ERP lento", Prioridade.ALTA, ana));
        Chamado fechado = repository.save(chamado("Servidor fora", Prioridade.CRITICA, ana));
        fechado.setStatus(StatusChamado.FECHADO);
        repository.save(chamado("Acesso VPN", Prioridade.ALTA, bruno));
        repository.flush();
    }

    private Chamado chamado(String titulo, Prioridade prioridade, Cliente cliente) {
        return new Chamado(titulo, "Descrição", prioridade, cliente, hardware, null);
    }
}
