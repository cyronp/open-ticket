package com.openticket.controller;

import com.openticket.dto.TecnicoRequest;
import com.openticket.dto.TecnicoResponse;
import com.openticket.entity.Perfil;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.TecnicoService;
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

@WebMvcTest(TecnicoController.class)
class TecnicoControllerTest {
    private static final String TECNICO_VALIDO = """
            {"nome": "João", "email": "joao@x.com", "perfil": "TECNICO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TecnicoService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar(any())).thenReturn(new TecnicoResponse(1L, "João", "joao@x.com", Perfil.TECNICO));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tecnicos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.perfil").value("TECNICO"));

        verify(service).criar(new TecnicoRequest("João", "joao@x.com", Perfil.TECNICO));
    }

    @Test
    void criarComCamposInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "email": "invalido"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: Nome é obrigatório!",
                        "email: Email inválido!",
                        "perfil: Perfil é obrigatório!"
                )));

        verifyNoInteractions(service);
    }

    @Test
    void criarComPerfilInexistenteRetorna400() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "João", "email": "joao@x.com", "perfil": "GERENTE"}
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe um técnico com o e-mail joao@x.com"));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Já existe um técnico com o e-mail joao@x.com"));
    }

    @Test
    void atualizarComEmailDuplicadoRetorna409() throws Exception {
        when(service.atualizar(eq(1L), any())).thenThrow(new RegraNegocioException("Já existe um técnico com o e-mail joao@x.com"));

        mockMvc.perform(put("/tecnicos/1").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Técnico 999 não encontrado!"));

        mockMvc.perform(get("/tecnicos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Técnico 999 não encontrado!"));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        when(service.atualizar(eq(999L), any())).thenThrow(new RecursoNaoEncontradoException("Técnico 999 não encontrado!"));

        mockMvc.perform(put("/tecnicos/999").contentType(MediaType.APPLICATION_JSON).content(TECNICO_VALIDO))
                .andExpect(status().isNotFound());
    }
}
