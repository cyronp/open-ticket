package com.openticket.dto;

import com.openticket.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record ChamadoStatusRequest(
        @NotNull(message = "Status é obrigatório!")
        StatusChamado status
) {}
