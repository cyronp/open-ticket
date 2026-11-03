package com.openticket.dto;

import com.openticket.entity.Comentario;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String texto, String autor, LocalDateTime dataCriacao) {
    public static ComentarioResponse de(Comentario comentario) {
        return new ComentarioResponse(comentario.getId(), comentario.getTexto(), comentario.getAutor(), comentario.getDataCriacao());
    }
}
