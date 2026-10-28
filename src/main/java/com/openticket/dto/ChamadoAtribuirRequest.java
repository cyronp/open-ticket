package com.openticket.dto;

import jakarta.validation.constraints.NotNull;

public record ChamadoAtribuirRequest(
        @NotNull(message = "Técnico é obrigatório!")
        Long tecnicoId
) {}
