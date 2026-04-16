package com.compilador.controller;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import com.compilador.service.EjecutarService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EjecutarController {

    private final EjecutarService ejecutarService;

    public EjecutarController(EjecutarService ejecutarService) {
        this.ejecutarService = ejecutarService;
    }

    @PostMapping("/ejecutar")
    public EjecutarResponse ejecutar(@RequestBody EjecutarRequest request) {
        return ejecutarService.ejecutar(request);
    }
}
