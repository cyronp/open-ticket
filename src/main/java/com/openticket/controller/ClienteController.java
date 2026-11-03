package com.openticket.controller;

import com.openticket.dto.ChamadoResponse;
import com.openticket.dto.ClienteRequest;
import com.openticket.dto.ClienteResponse;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import com.openticket.service.ChamadoService;
import com.openticket.service.ClienteService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cria(@Valid @RequestBody ClienteRequest dto) {
        ClienteResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }


    @GetMapping("/{id}")
    public ClienteResponse buscar(
            @PathVariable Long id
    ) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/chamados")
    public Page<ChamadoResponse> listarChamados(
            @PathVariable Long id,
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @PageableDefault(sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return chamadoService.listarPorCliente(id, status, prioridade, pageable);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest dto
    ) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
