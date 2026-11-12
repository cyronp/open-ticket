package com.openticket.web;

import com.openticket.entity.StatusChamado;
import com.openticket.service.CategoriaService;
import com.openticket.service.ChamadoService;
import com.openticket.service.ClienteService;
import com.openticket.service.TecnicoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class PainelViewController {
    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;
    private final CategoriaService categoriaService;

    public PainelViewController(
            ChamadoService chamadoService,
            ClienteService clienteService,
            TecnicoService tecnicoService,
            CategoriaService categoriaService
    ) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.tecnicoService = tecnicoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/app";
    }

    @GetMapping("/app")
    public String painel(Model model) {
        Map<StatusChamado, Long> porStatus = new LinkedHashMap<>();
        for (StatusChamado status : StatusChamado.values()) {
            porStatus.put(status, chamadoService.listar(status, null, null, PageRequest.of(0, 1)).getTotalElements());
        }

        model.addAttribute("porStatus", porStatus);
        model.addAttribute("recentes", chamadoService.listar(null, null, null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        model.addAttribute("totalClientes", clienteService.listar().size());
        model.addAttribute("totalTecnicos", tecnicoService.listar().size());
        model.addAttribute("totalCategorias", categoriaService.listar().size());
        return "painel";
    }
}
