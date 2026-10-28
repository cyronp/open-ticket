package com.openticket.controller;

import com.openticket.dto.CategoriaRequest;
import com.openticket.dto.CategoriaResponse;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {
    private static final String CATEGORIA_VALIDA = """
            {"nome": "Hardware", "descricao": "Equipamentos"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new CategoriaResponse(1L, "Hardware", "Equipamentos"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categorias/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Hardware"))
                .andExpect(jsonPath("$.descricao").value("Equipamentos"));

        verify(service).criar(new CategoriaRequest("Hardware", "Equipamentos"));
    }

    @Test
    void criarSemNomeRetorna400() throws Exception {
        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("""
                        {"descricao": "Equipamentos"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes[0]").value("nome: Nome é obrigatório!"));

        verifyNoInteractions(service);
    }

    @Test
    void criarComNomeMuitoLongoRetorna400() throws Exception {
        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"" + "a".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("nome: Nome deve ter no máximo 100 caracteres!"));

        verifyNoInteractions(service);
    }

    @Test
    void criarComNomeDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe uma categoria com o nome Hardware"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Já existe uma categoria com o nome Hardware"));
    }

    @Test
    void atualizarComNomeDuplicadoRetorna409() throws Exception {
        when(service.atualizar(eq(1L), any())).thenThrow(new RegraNegocioException("Já existe uma categoria com o nome Hardware"));

        mockMvc.perform(put("/categorias/1").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Categoria 999 não encontrada!"));

        mockMvc.perform(get("/categorias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Categoria 999 não encontrada!"));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        when(service.atualizar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Categoria 999 não encontrada!"));

        mockMvc.perform(put("/categorias/999").contentType(MediaType.APPLICATION_JSON).content(CATEGORIA_VALIDA))
                .andExpect(status().isNotFound());
    }
}
