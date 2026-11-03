package com.openticket.exception;

public class ChamadoFechadoException extends RuntimeException {
    public ChamadoFechadoException(Long id) {
        super("Chamado " + id + " está fechado e não pode ser alterado!");
    }
}
