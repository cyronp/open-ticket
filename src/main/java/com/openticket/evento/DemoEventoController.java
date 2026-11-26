package com.openticket.evento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints apenas para a demonstração: listar o outbox e simular um reprocessamento.
 */
@RestController
@RequestMapping("/demo/eventos")
public class DemoEventoController {

    private final EventoOutboxRepository outbox;

    public DemoEventoController(EventoOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return outbox.findAll().stream()
                .map(e -> Map.<String, Object>of(
                        "eventId", e.getEventId(),
                        "tipo", e.getTipo(),
                        "chamadoId", e.getChamadoId(),
                        "status", e.getStatus(),
                        "tentativas", e.getTentativas()))
                .toList();
    }

    @PostMapping("/{eventId}/reprocessar")
    public ResponseEntity<Void> reprocessar(@PathVariable String eventId) {
        return outbox.findByEventId(eventId)
                .map(e -> {
                    e.reprocessar();
                    outbox.save(e);
                    return ResponseEntity.accepted().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
