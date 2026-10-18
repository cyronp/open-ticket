package com.openticket.dto;

import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoAtualizacaoRequest(
        @NotBlank(message = "Título é obrigatório!")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres!")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória!")
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres!")
        String descricao,

        @NotNull(message = "Prioridade é obrigatória!")
        Prioridade prioridade,

        @NotNull(message = "Status é obrigatório!")
        StatusChamado status,

        @NotNull(message = "Categoria é obrigatória!")
        Long categoriaId,

        Long tecnicoId
){}
