package com.openticket.evento;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Registro dos eventos já processados pelo consumidor.
 * O eventId é a chave primária, então o mesmo evento nunca é registrado duas vezes.
 */
@Entity
@Table(name = "evento_processado")
public class EventoProcessado {

    @Id
    @Column(length = 36)
    private String eventId;

    private Instant processadoEm;

    protected EventoProcessado() {
    }

    public EventoProcessado(String eventId) {
        this.eventId = eventId;
        this.processadoEm = Instant.now();
    }

    public String getEventId() { return eventId; }
    public Instant getProcessadoEm() { return processadoEm; }
}
