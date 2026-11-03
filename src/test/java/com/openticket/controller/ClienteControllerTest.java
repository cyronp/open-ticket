package com.openticket.controller;

import com.openticket.dto.ClienteRequest;
import com.openticket.dto.ClienteResponse;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.ChamadoService;
import com.openticket.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {
    private static final String CLIENTE_VALIDO = """
            {"nome": "Ana Souza", "email": "ana@x.com", "telefone": "11999990000"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private ChamadoService chamadoService;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new ClienteResponse(1L, "Ana Souza", "ana@x.com", "11999990000"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/clientes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@x.com"));

        verify(service).criar(new ClienteRequest("Ana Souza", "ana@x.com", "11999990000"));
    }

    @Test
    void criarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "email": "invalido"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: Nome é obrigatório!",
                        "email: Email inválido!"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe um cliente com este e-mail ana@x.com"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Já existe um cliente com este e-mail ana@x.com"));
    }

    @Test
    void atualizarComEmailDuplicadoRetorna409() throws Exception {
        when(service.atualizar(eq(1L), any())).thenThrow(new RegraNegocioException("Já existe cliente com o e-mail ana@x.com"));

        mockMvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado!"));

        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Cliente 999 não encontrado!"));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        when(service.atualizar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado!"));

        mockMvc.perform(put("/clientes/999").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarChamadosDeClienteInexistenteRetorna404() throws Exception {
        when(chamadoService.listarPorCliente(eq(999L), any(), any(), any()))
                .thenThrow(new RecursoNaoEncontradoException("Cliente 999 não encontrado!"));

        mockMvc.perform(get("/clientes/999/chamados"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cliente 999 não encontrado!"));
    }
}
