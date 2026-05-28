package com.compilador.controller;

import com.compilador.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crud")
public class CrudController {

    private static final Logger log = LoggerFactory.getLogger(CrudController.class);
    private final CrudService crudService;

    public CrudController(CrudService crudService) {
        this.crudService = crudService;
    }

    private ResponseEntity<?> fail(Exception e) {
        log.error("CRUD error", e);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return ResponseEntity.internalServerError().body(Map.of("exito", false, "mensaje", msg));
    }

    @GetMapping("/{baseDatos}/{tabla}")
    public ResponseEntity<?> listar(@PathVariable String baseDatos, @PathVariable String tabla) {
        try {
            List<Map<String, Object>> rows = crudService.listar(baseDatos, tabla);
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return fail(e);
        }
    }

    @GetMapping("/{baseDatos}/{tabla}/columnas")
    public ResponseEntity<?> columnas(@PathVariable String baseDatos, @PathVariable String tabla) {
        try {
            List<String> cols = crudService.columnas(baseDatos, tabla);
            return ResponseEntity.ok(cols);
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PostMapping("/{baseDatos}/{tabla}")
    public ResponseEntity<?> insertar(@PathVariable String baseDatos, @PathVariable String tabla,
                                       @RequestBody Map<String, Object> data) {
        try {
            return ResponseEntity.ok(crudService.insertar(baseDatos, tabla, data));
        } catch (Exception e) {
            return fail(e);
        }
    }

    @PutMapping("/{baseDatos}/{tabla}/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String baseDatos, @PathVariable String tabla,
                                         @PathVariable String id, @RequestBody Map<String, Object> data) {
        try {
            return ResponseEntity.ok(crudService.actualizar(baseDatos, tabla, Integer.parseInt(id), data));
        } catch (Exception e) {
            return fail(e);
        }
    }

    @DeleteMapping("/{baseDatos}/{tabla}/{id}")
    public ResponseEntity<?> borrar(@PathVariable String baseDatos, @PathVariable String tabla,
                                     @PathVariable String id) {
        try {
            return ResponseEntity.ok(crudService.borrar(baseDatos, tabla, Integer.parseInt(id)));
        } catch (Exception e) {
            return fail(e);
        }
    }
}
