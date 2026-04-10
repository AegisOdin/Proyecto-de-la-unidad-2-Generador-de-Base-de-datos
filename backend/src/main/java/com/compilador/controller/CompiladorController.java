package com.compilador.controller;

import com.compilador.dto.CompiladorRequest;
import com.compilador.dto.CompiladorResponse;
import com.compilador.service.CompiladorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompiladorController {

    private final CompiladorService compiladorService;

    public CompiladorController(CompiladorService compiladorService) {
        this.compiladorService = compiladorService;
    }

    @PostMapping("/compilar")
    public CompiladorResponse compilar(@RequestBody CompiladorRequest request) {
        return compiladorService.compilar(request.getCodigo());
    }
}
