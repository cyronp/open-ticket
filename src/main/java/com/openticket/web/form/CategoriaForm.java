package com.openticket.web.form;

import com.openticket.dto.CategoriaRequest;
import com.openticket.dto.CategoriaResponse;

public class CategoriaForm {
    private String nome;
    private String descricao;

    public static CategoriaForm de(CategoriaResponse categoria) {
        CategoriaForm form = new CategoriaForm();
        form.nome = categoria.nome();
        form.descricao = categoria.descricao();
        return form;
    }

    public CategoriaRequest toRequest() {
        return new CategoriaRequest(nome, descricao);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
