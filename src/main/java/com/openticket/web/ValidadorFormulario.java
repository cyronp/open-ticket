package com.openticket.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

// Valida o DTO da API (records com as anotações) e joga as violações no BindingResult do formulário,
// assim as regras de validação continuam num lugar só
@Component
public class ValidadorFormulario {
    private final Validator validator;

    public ValidadorFormulario(Validator validator) {
        this.validator = validator;
    }

    public <T> T validar(T request, BindingResult result) {
        for (ConstraintViolation<T> violacao : validator.validate(request)) {
            String campo = violacao.getPropertyPath().toString();
            if (result.hasFieldErrors(campo)) {
                continue; // erro de conversão já registrado pelo Spring
            }
            result.rejectValue(campo, "invalido", violacao.getMessage());
        }
        return request;
    }
}
