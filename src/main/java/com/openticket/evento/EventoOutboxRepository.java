package com.openticket.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, Long> {

    List<EventoOutbox> findTop10ByStatusAndProximaTentativaLessThanEqualOrderByIdAsc(
            EventoOutbox.Status status, Instant agora);

    Optional<EventoOutbox> findByEventId(String eventId);
}
