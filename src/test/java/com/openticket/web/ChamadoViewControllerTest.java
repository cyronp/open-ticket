package com.openticket.web;

import com.openticket.dto.CategoriaResponse;
import com.openticket.dto.ChamadoDetalheResponse;
import com.openticket.dto.ChamadoResponse;
import com.openticket.dto.ChamadoStatusRequest;
import com.openticket.dto.ClienteResponse;
import com.openticket.dto.ComentarioRequest;
import com.openticket.dto.ComentarioResponse;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import com.openticket.exception.ChamadoFechadoException;
import com.openticket.service.CategoriaService;
import com.openticket.service.ChamadoService;
import com.openticket.service.ClienteService;
import com.openticket.service.TecnicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChamadoViewController.class)
@Import(ValidadorFormulario.class)
class ChamadoViewControllerTest {
    private static final ClienteResponse CLIENTE = new ClienteResponse(1L, "Ana Souza", "ana@x.com", null);
    private static final CategoriaResponse CATEGORIA = new CategoriaResponse(1L, "Rede", null);
    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 1, 15, 9, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService service;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private TecnicoService tecnicoService;

    @Test
    void listarMostraChamadosComRotulosTraduzidos() throws Exception {
        ChamadoResponse chamado = new ChamadoResponse(7L, "Sem internet", "desc", ABERTURA, null,
                Prioridade.CRITICA, StatusChamado.EM_ATENDIMENTO, CLIENTE, CATEGORIA, null);
        when(service.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(chamado), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/app/chamados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sem internet")))
                .andExpect(content().string(containsString("Em atendimento")))
                .andExpect(content().string(containsString("Crítica")))
                .andExpect(content().string(containsString("15/01/2026 09:30")))
                .andExpect(content().string(containsString("Não atribuído")));
    }

    @Test
    void listarRepassaFiltros() throws Exception {
        when(service.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/app/chamados").param("status", "ABERTO").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum chamado encontrado")));

        verify(service).listar(eq(StatusChamado.ABERTO), isNull(), eq(1L), any());
    }

    @Test
    void criarInvalidoMostraErros() throws Exception {
        mockMvc.perform(post("/app/chamados").param("titulo", "").param("prioridade", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/form"))
                .andExpect(model().attributeHasFieldErrors("form", "titulo", "descricao", "clienteId", "categoriaId"));

        verify(service, never()).criar(any());
    }

    @Test
    void criarValidoRedirecionaParaDetalhe() throws Exception {
        when(service.criar(any())).thenReturn(new ChamadoResponse(5L, "T", "D", ABERTURA, null,
                Prioridade.ALTA, StatusChamado.ABERTO, CLIENTE, CATEGORIA, null));

        mockMvc.perform(post("/app/chamados")
                        .param("titulo", "T").param("descricao", "D").param("prioridade", "ALTA")
                        .param("clienteId", "1").param("categoriaId", "1").param("tecnicoId", ""))
                .andExpect(redirectedUrl("/app/chamados/5"))
                .andExpect(flash().attribute("sucesso", "Chamado #5 aberto."));
    }

    @Test
    void detalheDeChamadoFechadoEscondeAcoes() throws Exception {
        when(service.buscar(3L)).thenReturn(detalhe(3L, StatusChamado.FECHADO));

        mockMvc.perform(get("/app/chamados/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chamado fechado não recebe novos comentários.")))
                .andExpect(content().string(not(containsString("Ações rápidas"))))
                .andExpect(content().string(containsString("Primeiro contato")));
    }

    @Test
    void editarChamadoFechadoRedirecionaComErro() throws Exception {
        when(service.buscar(3L)).thenReturn(detalhe(3L, StatusChamado.FECHADO));

        mockMvc.perform(get("/app/chamados/3/editar"))
                .andExpect(redirectedUrl("/app/chamados/3"))
                .andExpect(flash().attribute("erro", "Chamado fechado não pode ser editado."));
    }

    @Test
    void comentarValidoChamaService() throws Exception {
        mockMvc.perform(post("/app/chamados/3/comentarios").param("autor", "Ana").param("texto", "Oi"))
                .andExpect(redirectedUrl("/app/chamados/3#comentarios"));

        verify(service).comentar(3L, new ComentarioRequest("Oi", "Ana"));
    }

    @Test
    void comentarInvalidoRenderizaDetalheComErros() throws Exception {
        when(service.buscar(3L)).thenReturn(detalhe(3L, StatusChamado.ABERTO));

        mockMvc.perform(post("/app/chamados/3/comentarios").param("autor", "").param("texto", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/detalhe"))
                .andExpect(model().attributeHasFieldErrors("comentario", "autor", "texto"));
    }

    @Test
    void alterarStatusDeChamadoFechadoMostraErro() throws Exception {
        when(service.alterarStatus(eq(3L), any())).thenThrow(new ChamadoFechadoException(3L));

        mockMvc.perform(post("/app/chamados/3/status").param("status", "ABERTO"))
                .andExpect(redirectedUrl("/app/chamados/3"))
                .andExpect(flash().attributeExists("erro"));

        verify(service).alterarStatus(3L, new ChamadoStatusRequest(StatusChamado.ABERTO));
    }

    @Test
    void filtroInvalidoRenderizaPaginaDeErro() throws Exception {
        mockMvc.perform(get("/app/chamados").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("erro"));
    }

    private ChamadoDetalheResponse detalhe(Long id, StatusChamado status) {
        return new ChamadoDetalheResponse(id, "Sem internet", "desc", ABERTURA,
                status == StatusChamado.FECHADO ? ABERTURA.plusDays(1) : null,
                Prioridade.MEDIA, status, CLIENTE, CATEGORIA, null,
                List.of(new ComentarioResponse(1L, "Primeiro contato", "Bruno", ABERTURA)));
    }
}
