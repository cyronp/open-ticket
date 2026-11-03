package com.openticket.repository;

import com.openticket.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ComentarioRepositoryTest {
    @Autowired
    private ComentarioRepository repository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Chamado chamado;

    @BeforeEach
    void setUp() {
        Cliente cliente = clienteRepository.save(new Cliente("Ana Souza", "ana@x.com", null));
        Categoria categoria = categoriaRepository.save(new Categoria("Hardware", null));
        chamado = chamadoRepository.save(new Chamado("Mouse quebrado", "Não clica", Prioridade.BAIXA, cliente, categoria, null));
    }

    @Test
    void salvarGeraIdEDataCriacao() {
        Comentario salvo = repository.save(new Comentario("Verificando", "João", chamado));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getDataCriacao()).isNotNull();
    }

    @Test
    void salvarSemChamadoFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Comentario("Verificando", "João", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarSemTextoFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Comentario(null, "João", chamado)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void chamadoCarregaSeusComentarios() {
        repository.save(new Comentario("Verificando", "João", chamado));
        repository.save(new Comentario("Resolvido", "Ana Souza", chamado));
        entityManager.flush();
        entityManager.clear();

        Chamado carregado = chamadoRepository.findById(chamado.getId()).orElseThrow();

        assertThat(carregado.getComentarios())
                .extracting(Comentario::getTexto)
                .containsExactlyInAnyOrder("Verificando", "Resolvido");
    }

    @Test
    void excluirChamadoExcluiComentarios() {
        repository.save(new Comentario("Verificando", "João", chamado));
        entityManager.flush();
        entityManager.clear();

        chamadoRepository.delete(chamadoRepository.findById(chamado.getId()).orElseThrow());
        entityManager.flush();

        assertThat(repository.count()).isZero();
    }
}
