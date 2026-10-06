package com.openticket.service;

import com.openticket.dto.ClienteRequest;
import com.openticket.dto.ClienteResponse;
import com.openticket.entity.Cliente;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.repository.ClienteRepository;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("Já existe um cliente com este e-mail" + dto.email());
        }
        Cliente salvo = repository.save(new Cliente(dto.email(), dto.nome(), dto.telefone()));
        return ClienteResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntity(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest dto) {
        Cliente cliente = buscarEntity(id);
        if (repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new RegraNegocioException("Já existe cliente com o e-mail" + dto.email());
        }

        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Cliente buscarEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente" + id + "não encontrado!"));
    }
}
