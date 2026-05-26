package com.compilador.controller;

import com.compilador.dto.GenerarRequest;
import com.compilador.service.GeneradorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GeneradorController {

    private final GeneradorService generadorService;

    public GeneradorController(GeneradorService generadorService) {
        this.generadorService = generadorService;
    }

    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody GenerarRequest request) {
        try {
            byte[] zip = generadorService.generarZip(request.getBaseDatos(), request.getTabla());
            String filename = "crud_" + request.getTabla().nombre + ".zip";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zip);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }
}
