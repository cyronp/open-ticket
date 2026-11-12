package com.openticket.web.form;

import com.openticket.dto.ClienteRequest;
import com.openticket.dto.ClienteResponse;

public class ClienteForm {
    private String nome;
    private String email;
    private String telefone;

    public static ClienteForm de(ClienteResponse cliente) {
        ClienteForm form = new ClienteForm();
        form.nome = cliente.nome();
        form.email = cliente.email();
        form.telefone = cliente.telefone();
        return form;
    }

    public ClienteRequest toRequest() {
        return new ClienteRequest(nome, email, telefone);
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
