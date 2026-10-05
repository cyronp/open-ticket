package com.openticket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
public class NameController {

    @GetMapping("/name")
    public String name() {
        LocalDate now = LocalDate.now();
        return "VITOR HENRIQUE DA SILVEIRA ALANO - DAS - " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
