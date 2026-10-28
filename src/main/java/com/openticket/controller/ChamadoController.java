package com.openticket.controller;

import com.openticket.dto.ChamadoAtribuirRequest;
import com.openticket.dto.ChamadoAtualizacaoRequest;
import com.openticket.dto.ChamadoRequest;
import com.openticket.dto.ChamadoResponse;
import com.openticket.dto.ChamadoStatusRequest;
import com.openticket.dto.ComentarioRequest;
import com.openticket.dto.ComentarioResponse;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import com.openticket.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {
    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(@Valid @RequestBody ChamadoRequest dto) {
        ChamadoResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/chamados/" + criado.id())).body(criado);
    }

    @GetMapping
    public Page<ChamadoResponse> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.listar(status, prioridade, clienteId, pageable);
    }

    @GetMapping("/{id}")
    public ChamadoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioRequest dto
    ) {
        ComentarioResponse criado = service.comentar(id, dto);
        return ResponseEntity.created(URI.create("/chamados/" + id)).body(criado);
    }

    @PutMapping("/{id}")
    public ChamadoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoAtualizacaoRequest dto
    ) {
        return service.atualizar(id, dto);
    }

    @PatchMapping("/{id}/atribuir")
    public ChamadoResponse atribuir(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoAtribuirRequest dto
    ) {
        return service.atribuir(id, dto);
    }

    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoStatusRequest dto
    ) {
        return service.alterarStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
