package com.openticket.repository;

import com.openticket.entity.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CategoriaRepositoryTest {
    @Autowired
    private CategoriaRepository repository;

    @Test
    void salvarGeraId() {
        Categoria salva = repository.save(new Categoria("Hardware", "Equipamentos"));

        assertThat(salva.getId()).isNotNull();
        assertThat(repository.findById(salva.getId())).get()
                .extracting(Categoria::getNome, Categoria::getDescricao)
                .containsExactly("Hardware", "Equipamentos");
    }

    @Test
    void salvarSemNomeFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Categoria(null, "Equipamentos")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarNomeDuplicadoFalha() {
        repository.saveAndFlush(new Categoria("Hardware", null));

        assertThatThrownBy(() -> repository.saveAndFlush(new Categoria("Hardware", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByNomeIgnoreCase() {
        repository.save(new Categoria("Hardware", null));

        assertThat(repository.existsByNomeIgnoreCase("hardware")).isTrue();
        assertThat(repository.existsByNomeIgnoreCase("HARDWARE")).isTrue();
        assertThat(repository.existsByNomeIgnoreCase("Software")).isFalse();
    }

    @Test
    void existsByNomeIgnoreCaseAndIdNotIgnoraAPropriaCategoria() {
        Categoria hardware = repository.save(new Categoria("Hardware", null));
        Categoria software = repository.save(new Categoria("Software", null));

        assertThat(repository.existsByNomeIgnoreCaseAndIdNot("hardware", hardware.getId())).isFalse();
        assertThat(repository.existsByNomeIgnoreCaseAndIdNot("hardware", software.getId())).isTrue();
    }

    @Test
    void buscarInexistenteRetornaVazio() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}
