package com.openticket.dto;

import com.openticket.entity.Chamado;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        ClienteResponse cliente,
        CategoriaResponse categoria,
        TecnicoResponse tecnico
) {
    public static ChamadoResponse de(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                ClienteResponse.de(chamado.getCliente()),
                CategoriaResponse.de(chamado.getCategoria()),
                chamado.getTecnico() != null ? TecnicoResponse.de(chamado.getTecnico()) : null
        );
    }
}
