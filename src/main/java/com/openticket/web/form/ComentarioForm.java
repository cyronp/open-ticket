package com.openticket.web.form;

import com.openticket.dto.ComentarioRequest;

public class ComentarioForm {
    private String texto;
    private String autor;

    public ComentarioRequest toRequest() {
        return new ComentarioRequest(texto, autor);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}
