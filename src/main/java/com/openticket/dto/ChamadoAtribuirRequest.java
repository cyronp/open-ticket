package com.openticket.dto;

import jakarta.validation.constraints.NotBlank;


public record ChamadoAtribuirRequest(
        @NotBlank(message = "Técnico é obrigatório")
        Long idTecnico
        
) {
}
