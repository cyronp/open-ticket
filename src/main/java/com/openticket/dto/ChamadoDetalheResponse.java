package com.openticket.dto;

import com.openticket.entity.Chamado;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

// Usado só no GET /chamados/{id}: a listagem não carrega comentários para não fazer uma consulta por chamado
public record ChamadoDetalheResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        ClienteResponse cliente,
        CategoriaResponse categoria,
        TecnicoResponse tecnico,
        List<ComentarioResponse> comentarios
) {
    public static ChamadoDetalheResponse de(Chamado chamado) {
        return new ChamadoDetalheResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                ClienteResponse.de(chamado.getCliente()),
                CategoriaResponse.de(chamado.getCategoria()),
                chamado.getTecnico() != null ? TecnicoResponse.de(chamado.getTecnico()) : null,
                chamado.getComentarios().stream().map(ComentarioResponse::de).toList()
        );
    }
}
