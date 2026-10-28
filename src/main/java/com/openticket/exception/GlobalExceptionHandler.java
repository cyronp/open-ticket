package com.openticket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException ex) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.toList());
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    @ExceptionHandler(OrdenacaoInvalidaException.class)
    public ResponseEntity<ErroResposta> ordenacaoInvalida(OrdenacaoInvalidaException ex) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    // Ex.: ?status=QUALQUER ou ?clienteId=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro " + ex.getName() + ": " + ex.getValue(), List.of());
    }

    @ExceptionHandler(ChamadoFechadoException.class)
    public ResponseEntity<ErroResposta> chamadoFechado(ChamadoFechadoException ex) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    private ResponseEntity<ErroResposta> montar(
            HttpStatus status, String erro, List<String> detalhes
    ) {
        return ResponseEntity.status(status).body(new ErroResposta(LocalDateTime.now(), status.value(), erro, detalhes));
    }
}
