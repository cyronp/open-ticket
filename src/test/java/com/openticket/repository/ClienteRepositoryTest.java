package com.openticket.repository;

import com.openticket.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ClienteRepositoryTest {
    @Autowired
    private ClienteRepository repository;

    @Test
    void salvarGeraId() {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@x.com", "11999990000"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repository.findById(salvo.getId())).get()
                .extracting(Cliente::getNome, Cliente::getEmail, Cliente::getTelefone)
                .containsExactly("Ana Souza", "ana@x.com", "11999990000");
    }

    @Test
    void salvarSemNomeFalha() {
        assertThatThrownBy(() -> repository.saveAndFlush(new Cliente(null, "ana@x.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void salvarEmailDuplicadoFalha() {
        repository.saveAndFlush(new Cliente("Ana Souza", "ana@x.com", null));

        assertThatThrownBy(() -> repository.saveAndFlush(new Cliente("Outra Ana", "ana@x.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByEmail() {
        repository.save(new Cliente("Ana Souza", "ana@x.com", null));

        assertThat(repository.existsByEmail("ana@x.com")).isTrue();
        assertThat(repository.existsByEmail("bruno@x.com")).isFalse();
    }

    @Test
    void existsByEmailAndIdNotIgnoraOProprioCliente() {
        Cliente ana = repository.save(new Cliente("Ana Souza", "ana@x.com", null));
        Cliente bruno = repository.save(new Cliente("Bruno Lima", "bruno@x.com", null));

        assertThat(repository.existsByEmailAndIdNot("ana@x.com", ana.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("ana@x.com", bruno.getId())).isTrue();
    }

    @Test
    void buscarInexistenteRetornaVazio() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}
