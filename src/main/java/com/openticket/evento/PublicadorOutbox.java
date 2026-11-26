package com.openticket.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Lê periodicamente os eventos PENDENTE do outbox e tenta entregá-los.
 * Em caso de falha, agenda nova tentativa (backoff + jitter) ou marca FALHA.
 */
@Component
public class PublicadorOutbox {

    private static final Logger log = LoggerFactory.getLogger(PublicadorOutbox.class);

    private final EventoOutboxRepository outbox;
    private final NotificadorEmailSimulado notificador;

    public PublicadorOutbox(EventoOutboxRepository outbox, NotificadorEmailSimulado notificador) {
        this.outbox = outbox;
        this.notificador = notificador;
    }

    @Scheduled(fixedDelay = 2000)
    public void publicarPendentes() {
        var pendentes = outbox.findTop10ByStatusAndProximaTentativaLessThanEqualOrderByIdAsc(
                EventoOutbox.Status.PENDENTE, Instant.now());

        for (EventoOutbox evento : pendentes) {
            log.info("[PUBLICADOR] eventId={} tipo={} tentativa={}",
                    evento.getEventId(), evento.getTipo(), evento.getTentativas() + 1);
            try {
                notificador.entregar(evento);
                evento.marcarEnviado();
                log.info("[PUBLICADOR] eventId={} -> ENVIADO", evento.getEventId());
            } catch (RuntimeException ex) {
                evento.registrarFalha();
                if (evento.getStatus() == EventoOutbox.Status.FALHA) {
                    log.warn("[PUBLICADOR] eventId={} -> FALHA após {} tentativas ({})",
                            evento.getEventId(), evento.getTentativas(), ex.getMessage());
                } else {
                    log.warn("[PUBLICADOR] eventId={} falhou ({}). Nova tentativa em {}",
                            evento.getEventId(), ex.getMessage(), evento.getProximaTentativa());
                }
            }
            outbox.save(evento);
        }
    }
}
