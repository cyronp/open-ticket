package com.openticket.web;

import com.openticket.dto.ClienteRequest;
import com.openticket.dto.ClienteResponse;
import com.openticket.exception.RecursoNaoEncontradoException;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ClienteViewController.class)
@Import(ValidadorFormulario.class)
class ClienteViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void listarRenderizaTabela() throws Exception {
        when(service.listar()).thenReturn(List.of(new ClienteResponse(1L, "Ana Souza", "ana@x.com", null)));

        mockMvc.perform(get("/app/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/lista"))
                .andExpect(content().string(containsString("Ana Souza")))
                .andExpect(content().string(containsString("ana@x.com")));
    }

    @Test
    void criarValidoRedirecionaComMensagem() throws Exception {
        mockMvc.perform(post("/app/clientes")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Ana Souza")
                        .param("email", "ana@x.com")
                        .param("telefone", "11999990000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/clientes"))
                .andExpect(flash().attribute("sucesso", "Cliente cadastrado."));

        verify(service).criar(new ClienteRequest("Ana Souza", "ana@x.com", "11999990000"));
    }

    @Test
    void criarInvalidoMostraErrosDosCamposSemChamarService() throws Exception {
        mockMvc.perform(post("/app/clientes").param("nome", "").param("email", "invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/form"))
                .andExpect(model().attributeHasFieldErrors("form", "nome", "email"))
                .andExpect(content().string(containsString("Nome é obrigatório!")))
                .andExpect(content().string(containsString("Email inválido!")));

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoMostraErroGlobal() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe um cliente com este e-mail"));

        mockMvc.perform(post("/app/clientes").param("nome", "Ana").param("email", "ana@x.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/form"))
                .andExpect(model().attributeHasErrors("form"))
                .andExpect(content().string(containsString("Já existe um cliente com este e-mail")));
    }

    @Test
    void editarPreencheFormulario() throws Exception {
        when(service.buscar(1L)).thenReturn(new ClienteResponse(1L, "Ana Souza", "ana@x.com", "1199"));

        mockMvc.perform(get("/app/clientes/1/editar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Ana Souza\"")))
                .andExpect(content().string(containsString("Editar cliente")));
    }

    @Test
    void atualizarChamaService() throws Exception {
        mockMvc.perform(post("/app/clientes/1").param("nome", "Ana").param("email", "ana@x.com"))
                .andExpect(redirectedUrl("/app/clientes"));

        verify(service).atualizar(eq(1L), eq(new ClienteRequest("Ana", "ana@x.com", null)));
    }

    @Test
    void excluirComChamadosVinculadosMostraErro() throws Exception {
        doThrow(new RegraNegocioException("Cliente 1 possui chamados vinculados"))
                .when(service).excluir(1L);

        mockMvc.perform(post("/app/clientes/1/excluir"))
                .andExpect(redirectedUrl("/app/clientes"))
                .andExpect(flash().attribute("erro", "Cliente 1 possui chamados vinculados"));
    }

    @Test
    void clienteInexistenteRenderizaPaginaDeErroEmHtml() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99 não encontrado!"));

        mockMvc.perform(get("/app/clientes/99/editar"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erro"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Cliente 99 não encontrado!")));
    }
}
