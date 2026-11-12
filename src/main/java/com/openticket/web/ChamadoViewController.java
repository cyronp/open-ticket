package com.openticket.web;

import com.openticket.dto.ChamadoAtribuirRequest;
import com.openticket.dto.ChamadoAtualizacaoRequest;
import com.openticket.dto.ChamadoDetalheResponse;
import com.openticket.dto.ChamadoRequest;
import com.openticket.dto.ChamadoResponse;
import com.openticket.dto.ChamadoStatusRequest;
import com.openticket.dto.ComentarioRequest;
import com.openticket.entity.Prioridade;
import com.openticket.entity.StatusChamado;
import com.openticket.exception.ChamadoFechadoException;
import com.openticket.service.CategoriaService;
import com.openticket.service.ChamadoService;
import com.openticket.service.ClienteService;
import com.openticket.service.TecnicoService;
import com.openticket.web.form.ChamadoForm;
import com.openticket.web.form.ComentarioForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/chamados")
public class ChamadoViewController {
    private final ChamadoService service;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ValidadorFormulario validador;

    public ChamadoViewController(
            ChamadoService service,
            ClienteService clienteService,
            CategoriaService categoriaService,
            TecnicoService tecnicoService,
            ValidadorFormulario validador
    ) {
        this.service = service;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.validador = validador;
    }

    @ModelAttribute
    public void enums(Model model) {
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("statuses", StatusChamado.values());
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<ChamadoResponse> pagina = service.listar(status, prioridade, clienteId, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("status", status);
        model.addAttribute("prioridade", prioridade);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        ChamadoForm form = new ChamadoForm();
        form.setClienteId(clienteId);
        model.addAttribute("form", form);
        carregarOpcoes(model);
        return "chamados/form";
    }

    @PostMapping
    public String criar(@ModelAttribute("form") ChamadoForm form, BindingResult result, Model model,
                        RedirectAttributes redirect) {
        ChamadoRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            carregarOpcoes(model);
            return "chamados/form";
        }
        ChamadoResponse criado = service.criar(dto);
        redirect.addFlashAttribute("sucesso", "Chamado #" + criado.id() + " aberto.");
        return "redirect:/app/chamados/" + criado.id();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("comentario", new ComentarioForm());
        carregarDetalhe(id, model);
        return "chamados/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        ChamadoDetalheResponse chamado = service.buscar(id);
        if (chamado.status() == StatusChamado.FECHADO) {
            redirect.addFlashAttribute("erro", "Chamado fechado não pode ser editado.");
            return "redirect:/app/chamados/" + id;
        }
        model.addAttribute("id", id);
        model.addAttribute("chamado", chamado);
        model.addAttribute("form", ChamadoForm.de(chamado));
        carregarOpcoes(model);
        return "chamados/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute("form") ChamadoForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        ChamadoAtualizacaoRequest dto = validador.validar(form.toAtualizacao(), result);
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("chamado", service.buscar(id));
            carregarOpcoes(model);
            return "chamados/form";
        }
        try {
            service.atualizar(id, dto);
            redirect.addFlashAttribute("sucesso", "Chamado atualizado.");
        } catch (ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusChamado status, RedirectAttributes redirect) {
        try {
            service.alterarStatus(id, new ChamadoStatusRequest(status));
            redirect.addFlashAttribute("sucesso", "Status atualizado.");
        } catch (ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id, @RequestParam Long tecnicoId, RedirectAttributes redirect) {
        try {
            service.atribuir(id, new ChamadoAtribuirRequest(tecnicoId));
            redirect.addFlashAttribute("sucesso", "Técnico atribuído.");
        } catch (ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id, @ModelAttribute("comentario") ComentarioForm form,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        ComentarioRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            carregarDetalhe(id, model);
            return "chamados/detalhe";
        }
        try {
            service.comentar(id, dto);
            redirect.addFlashAttribute("sucesso", "Comentário adicionado.");
        } catch (ChamadoFechadoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/chamados/" + id + "#comentarios";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Chamado #" + id + " excluído.");
        return "redirect:/app/chamados";
    }

    private void carregarDetalhe(Long id, Model model) {
        model.addAttribute("chamado", service.buscar(id));
        model.addAttribute("tecnicos", tecnicoService.listar());
    }

    private void carregarOpcoes(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
    }
}
