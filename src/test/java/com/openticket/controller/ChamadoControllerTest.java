package com.openticket.controller;

import com.openticket.dto.*;
import com.openticket.entity.Perfil;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.service.ChamadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {
    private static final String CHAMADO_VALIDO = """
            {
              "titulo": "Mouse quebrado",
              "descricao": "Não clica",
              "prioridade": "BAIXA",
              "clienteId": 1,
              "categoriaId": 1
            }
            """;
    private static final String COMENTARIO_VALIDO = """
            {"texto": "Verificando", "autor": "João"}
            """;
    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 10, 27, 9, 0);

    private static final ClienteResponse CLIENTE = new ClienteResponse(1L, "Ana Souza", "ana@x.com", null);
    private static final CategoriaResponse CATEGORIA = new CategoriaResponse(1L, "Hardware", null);
    private static final TecnicoResponse TECNICO = new TecnicoResponse(1L, "João", "joao@x.com", Perfil.TECNICO);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new ChamadoResponse(
                1L, "Mouse quebrado", "Não clica", ABERTURA, null, Prioridade.BAIXA, StatusChamado.ABERTO, CLIENTE, CATEGORIA, null
        ));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/chamados/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.cliente.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.tecnico").doesNotExist());

        verify(service).criar(new ChamadoRequest("Mouse quebrado", "Não clica", Prioridade.BAIXA, 1L, 1L, null));
    }

    @Test
    void criarSemCamposObrigatoriosRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "titulo: Título é obrigatório!",
                        "descricao: Descrição é obrigatória!",
                        "prioridade: Prioridade é obrigatória!",
                        "clienteId: Cliente é obrigatório!",
                        "categoriaId: Categoria é obrigatória!"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComTituloMuitoLongoRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(
                        CHAMADO_VALIDO.replace("Mouse quebrado", "a".repeat(151))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("titulo: Título deve ter no máximo 150 caracteres!"));

        verifyNoInteractions(service);
    }

    @Test
    void criarComPrioridadeInexistenteRetorna400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(
                        CHAMADO_VALIDO.replace("BAIXA", "URGENTE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void criarComClienteInexistenteRetorna404() throws Exception {
        when(service.criar(any())).thenThrow(new RecursoNaoEncontradoException("Cliente 1 não encontrado!"));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Cliente 1 não encontrado!"));
    }

    @Test
    void buscarRetornaDetalheComComentarios() throws Exception {
        when(service.buscar(1L)).thenReturn(new ChamadoDetalheResponse(
                1L, "Mouse quebrado", "Não clica", ABERTURA, null, Prioridade.BAIXA, StatusChamado.EM_ATENDIMENTO,
                CLIENTE, CATEGORIA, TECNICO,
                List.of(
                        new ComentarioResponse(1L, "Verificando", "João", ABERTURA.plusHours(1)),
                        new ComentarioResponse(2L, "Resolvido", "Ana Souza", ABERTURA.plusHours(2))
                )
        ));

        mockMvc.perform(get("/chamados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tecnico.nome").value("João"))
                .andExpect(jsonPath("$.comentarios", hasSize(2)))
                .andExpect(jsonPath("$.comentarios[0].texto").value("Verificando"))
                .andExpect(jsonPath("$.comentarios[1].autor").value("Ana Souza"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Chamado 999 não encontrado!"));

        mockMvc.perform(get("/chamados/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Chamado 999 não encontrado!"));
    }

    @Test
    void comentarRetorna201() throws Exception {
        when(service.comentar(eq(1L), any())).thenReturn(new ComentarioResponse(1L, "Verificando", "João", ABERTURA));

        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON).content(COMENTARIO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/chamados/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.texto").value("Verificando"))
                .andExpect(jsonPath("$.autor").value("João"))
                .andExpect(jsonPath("$.dataCriacao").value("2026-10-27T09:00:00"));

        verify(service).comentar(1L, new ComentarioRequest("Verificando", "João"));
    }

    @Test
    void comentarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON).content("""
                        {"texto": "", "autor": " "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "texto: Texto é obrigatório!",
                        "autor: Autor é obrigatório!"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void comentarEmChamadoInexistenteRetorna404() throws Exception {
        when(service.comentar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Chamado 999 não encontrado!"));

        mockMvc.perform(post("/chamados/999/comentarios").contentType(MediaType.APPLICATION_JSON).content(COMENTARIO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Chamado 999 não encontrado!"));
    }
}
