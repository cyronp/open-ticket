package com.openticket.dto;

import com.openticket.entity.Perfil;
import com.openticket.entity.Tecnico;

public record TecnicoResponse(Long id, String nome, String email, Perfil perfil) {
    public static TecnicoResponse de(Tecnico tecnico) {
        return new TecnicoResponse(tecnico.getId(), tecnico.getNome(), tecnico.getEmail(), tecnico.getPerfil());
    }
}
