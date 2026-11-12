package com.openticket.web.form;

import com.openticket.dto.ChamadoAtualizacaoRequest;
import com.openticket.dto.ChamadoDetalheResponse;
import com.openticket.dto.ChamadoRequest;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;

// Um formulário só para abrir e editar: cliente só aparece na abertura e status só na edição
public class ChamadoForm {
    private String titulo;
    private String descricao;
    private Prioridade prioridade = Prioridade.MEDIA;
    private StatusChamado status;
    private Long clienteId;
    private Long categoriaId;
    private Long tecnicoId;

    public static ChamadoForm de(ChamadoDetalheResponse chamado) {
        ChamadoForm form = new ChamadoForm();
        form.titulo = chamado.titulo();
        form.descricao = chamado.descricao();
        form.prioridade = chamado.prioridade();
        form.status = chamado.status();
        form.clienteId = chamado.cliente().id();
        form.categoriaId = chamado.categoria().id();
        form.tecnicoId = chamado.tecnico() != null ? chamado.tecnico().id() : null;
        return form;
    }

    public ChamadoRequest toRequest() {
        return new ChamadoRequest(titulo, descricao, prioridade, clienteId, categoriaId, tecnicoId);
    }

    public ChamadoAtualizacaoRequest toAtualizacao() {
        return new ChamadoAtualizacaoRequest(titulo, descricao, prioridade, status, categoriaId, tecnicoId);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }
}
