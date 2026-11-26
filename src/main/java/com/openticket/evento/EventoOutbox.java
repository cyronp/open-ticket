package com.openticket.evento;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento gravado na mesma transação do chamado (padrão outbox).
 * O PublicadorOutbox lê os eventos PENDENTE e tenta entregá-los.
 */
@Entity
@Table(name = "evento_outbox")
public class EventoOutbox {

    public enum Status { PENDENTE, ENVIADO, FALHA }

    public static final int MAX_TENTATIVAS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Long chamadoId;

    // Apenas dados mínimos (LGPD): sem nome, e-mail, telefone ou descrição.
    @Column(nullable = false, length = 1000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private int tentativas;

    private Instant proximaTentativa;

    private Instant criadoEm;

    protected EventoOutbox() {
    }

    public static EventoOutbox novo(String tipo, Long chamadoId, String prioridade) {
        EventoOutbox e = new EventoOutbox();
        e.eventId = UUID.randomUUID().toString();
        e.tipo = tipo;
        e.chamadoId = chamadoId;
        e.criadoEm = Instant.now();
        e.payload = "{\"eventId\":\"%s\",\"tipo\":\"%s\",\"ocorridoEm\":\"%s\",\"dados\":{\"chamadoId\":%d,\"prioridade\":\"%s\",\"link\":\"/app/chamados/%d\"}}"
                .formatted(e.eventId, tipo, e.criadoEm, chamadoId, prioridade, chamadoId);
        e.status = Status.PENDENTE;
        e.tentativas = 0;
        e.proximaTentativa = e.criadoEm;
        return e;
    }

    public void marcarEnviado() {
        status = Status.ENVIADO;
    }

    /** Backoff exponencial com jitter. Intervalos encurtados para a demonstração: ~5s, 10s, 20s, 40s. */
    public void registrarFalha() {
        tentativas++;
        if (tentativas >= MAX_TENTATIVAS) {
            status = Status.FALHA;
            return;
        }
        long baseMs = 5_000L * (1L << (tentativas - 1));
        double jitter = 0.8 + Math.random() * 0.4; // variação de ±20%
        proximaTentativa = Instant.now().plusMillis((long) (baseMs * jitter));
    }

    /** Reprocessamento manual: volta para PENDENTE mantendo o MESMO eventId. */
    public void reprocessar() {
        status = Status.PENDENTE;
        tentativas = 0;
        proximaTentativa = Instant.now();
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTipo() { return tipo; }
    public Long getChamadoId() { return chamadoId; }
    public String getPayload() { return payload; }
    public Status getStatus() { return status; }
    public int getTentativas() { return tentativas; }
    public Instant getProximaTentativa() { return proximaTentativa; }
    public Instant getCriadoEm() { return criadoEm; }
}
