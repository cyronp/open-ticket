package com.openticket.web.form;

import com.openticket.dto.TecnicoRequest;
import com.openticket.dto.TecnicoResponse;
import com.openticket.entity.Perfil;

public class TecnicoForm {
    private String nome;
    private String email;
    private Perfil perfil;

    public static TecnicoForm de(TecnicoResponse tecnico) {
        TecnicoForm form = new TecnicoForm();
        form.nome = tecnico.nome();
        form.email = tecnico.email();
        form.perfil = tecnico.perfil();
        return form;
    }

    public TecnicoRequest toRequest() {
        return new TecnicoRequest(nome, email, perfil);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
