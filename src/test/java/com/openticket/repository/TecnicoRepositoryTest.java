package com.openticket.repository;

import com.openticket.entity.Perfil;
import com.openticket.entity.Tecnico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TecnicoRepositoryTest {
    @Autowired
    private TecnicoRepository repository;

    @Test
    void salvarGeraId() {
        Tecnico salvo = repository.save(new Tecnico("João", "joao@x.com", Perfil.TECNICO));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repository.findById(salvo.getId())).get()
                .extracting(Tecnico::getNome, Tecnico::getEmail, Tecnico::getPerfil)
                .containsExactly("João", "joao@x.com", Perfil.TECNICO);
    }

    @Test
    void salvarSemPerfilFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Tecnico("João", "joao@x.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarEmailDuplicadoFalha() {
        repository.saveAndFlush(new Tecnico("João", "joao@x.com", Perfil.TECNICO));

        assertThatThrownBy(() -> repository.saveAndFlush(new Tecnico("Outro João", "joao@x.com", Perfil.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByEmailIgnoreCase() {
        repository.save(new Tecnico("João", "joao@x.com", Perfil.TECNICO));

        assertThat(repository.existsByEmailIgnoreCase("JOAO@x.com")).isTrue();
        assertThat(repository.existsByEmailIgnoreCase("marta@x.com")).isFalse();
    }

    @Test
    void existsByEmailIgnoreCaseAndIdNotIgnoraOProprioTecnico() {
        Tecnico joao = repository.save(new Tecnico("João", "joao@x.com", Perfil.TECNICO));
        Tecnico marta = repository.save(new Tecnico("Marta", "marta@x.com", Perfil.ADMIN));

        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("JOAO@x.com", joao.getId())).isFalse();
        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("JOAO@x.com", marta.getId())).isTrue();
    }

    @Test
    void buscarInexistenteRetornaVazio() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}
