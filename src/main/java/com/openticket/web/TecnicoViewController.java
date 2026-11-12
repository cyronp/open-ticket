package com.openticket.web;

import com.openticket.dto.TecnicoRequest;
import com.openticket.entity.Perfil;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.TecnicoService;
import com.openticket.web.form.TecnicoForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/tecnicos")
public class TecnicoViewController {
    private final TecnicoService service;
    private final ValidadorFormulario validador;

    public TecnicoViewController(TecnicoService service, ValidadorFormulario validador) {
        this.service = service;
        this.validador = validador;
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return Perfil.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        TecnicoForm form = new TecnicoForm();
        form.setPerfil(Perfil.TECNICO);
        model.addAttribute("form", form);
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@ModelAttribute("form") TecnicoForm form, BindingResult result, RedirectAttributes redirect) {
        TecnicoRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "tecnicos/form";
        }
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "tecnicos/form";
        }
        redirect.addFlashAttribute("sucesso", "Técnico cadastrado.");
        return "redirect:/app/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("form", TecnicoForm.de(service.buscar(id)));
        return "tecnicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute("form") TecnicoForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        TecnicoRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "tecnicos/form";
        }
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "tecnicos/form";
        }
        redirect.addFlashAttribute("sucesso", "Técnico atualizado.");
        return "redirect:/app/tecnicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("sucesso", "Técnico excluído.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/tecnicos";
    }
}
