package com.openticket.web;

import com.openticket.dto.CategoriaRequest;
import com.openticket.exception.RegraNegocioException;
import com.openticket.service.CategoriaService;
import com.openticket.web.form.CategoriaForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/categorias")
public class CategoriaViewController {
    private final CategoriaService service;
    private final ValidadorFormulario validador;

    public CategoriaViewController(CategoriaService service, ValidadorFormulario validador) {
        this.service = service;
        this.validador = validador;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CategoriaForm());
        return "categorias/form";
    }

    @PostMapping
    public String criar(@ModelAttribute("form") CategoriaForm form, BindingResult result, RedirectAttributes redirect) {
        CategoriaRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "categorias/form";
        }
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "categorias/form";
        }
        redirect.addFlashAttribute("sucesso", "Categoria cadastrada.");
        return "redirect:/app/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("form", CategoriaForm.de(service.buscar(id)));
        return "categorias/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute("form") CategoriaForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        CategoriaRequest dto = validador.validar(form.toRequest(), result);
        if (result.hasErrors()) {
            return "categorias/form";
        }
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "categorias/form";
        }
        redirect.addFlashAttribute("sucesso", "Categoria atualizada.");
        return "redirect:/app/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("sucesso", "Categoria excluída.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/app/categorias";
    }
}
