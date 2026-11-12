package com.openticket.web;

import com.openticket.dto.ClienteRequest;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.ClienteService;
import com.openticket.web.form.ClienteForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/clientes")
public class ClienteViewController {
    private final ClienteService service;
    private final ValidadorFormulario validador;

    public ClienteViewController(ClienteService service, ValidadorFormulario validador) {
        this.service = service;
        this.validador = validador;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteForm());
        return "clientes/form";
    }

    @PostMapping
    public String criar(@ModelAttribute("form") ClienteForm form, BindingResult result, RedirectAttributes redirect) {
        ClienteRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "clientes/form";
        }
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "clientes/form";
        }
        redirect.addFlashAttribute("sucesso", "Cliente cadastrado.");
        return "redirect:/app/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("form", ClienteForm.de(service.buscar(id)));
        return "clientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute("form") ClienteForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        ClienteRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "clientes/form";
        }
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "clientes/form";
        }
        redirect.addFlashAttribute("sucesso", "Cliente atualizado.");
        return "redirect:/app/clientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/clientes";
    }
}
