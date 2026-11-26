package com.openticket.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumidor idempotente. Simula o envio de e-mail apenas escrevendo no log.
 * Para a demonstração, a 1ª tentativa de cada evento falha de propósito.
 */
@Component
public class NotificadorEmailSimulado {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEmailSimulado.class);

    private final EventoProcessadoRepository processados;
    private final Set<String> jaFalharam = ConcurrentHashMap.newKeySet();

    @Value("${demo.simular-falha:true}")
    private boolean simularFalha;

    public NotificadorEmailSimulado(EventoProcessadoRepository processados) {
        this.processados = processados;
    }

    public void entregar(EventoOutbox evento) {
        String eventId = evento.getEventId();

        // Idempotência: evento já processado é descartado.
        if (processados.existsById(eventId)) {
            log.info("[CONSUMIDOR] eventId={} já processado -> DUPLICADO DESCARTADO", eventId);
            return;
        }

        // Falha simulada na primeira tentativa de cada evento.
        if (simularFalha && jaFalharam.add(eventId)) {
            throw new IllegalStateException("provedor de e-mail indisponível (simulado)");
        }

        log.info("[CONSUMIDOR] eventId={} -> e-mail enviado (simulado): chamado #{} aberto, link /app/chamados/{}",
                eventId, evento.getChamadoId(), evento.getChamadoId());
        processados.save(new EventoProcessado(eventId));
    }
}
