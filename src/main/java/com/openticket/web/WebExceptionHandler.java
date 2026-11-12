package com.openticket.web;

import com.openticket.exception.OrdenacaoInvalidaException;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

// O GlobalExceptionHandler responde JSON e vale para qualquer controller.
// Este roda antes, mas só para as telas, e devolve uma página de erro
@ControllerAdvice(basePackageClasses = WebExceptionHandler.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regraNegocio(RegraNegocioException ex) {
        return erro(HttpStatus.CONFLICT, "Operação não permitida", ex.getMessage());
    }

    @ExceptionHandler({
            OrdenacaoInvalidaException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ModelAndView requisicaoInvalida(Exception ex) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida", "Os parâmetros informados não são válidos.");
    }

    private ModelAndView erro(HttpStatus status, String titulo, String mensagem) {
        ModelAndView mav = new ModelAndView("erro");
        mav.setStatus(status);
        mav.addObject("codigo", status.value());
        mav.addObject("titulo", titulo);
        mav.addObject("mensagem", mensagem);
        return mav;
    }
}
