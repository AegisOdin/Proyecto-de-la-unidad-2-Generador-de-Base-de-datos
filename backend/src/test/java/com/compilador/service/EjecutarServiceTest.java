package com.compilador.service;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EjecutarServiceTest {

    @Test
    void ejecutar_retornaFalso_cuandoLasCredencialesSonInvalidas() {
        EjecutarService service = new EjecutarService();

        EjecutarRequest request = new EjecutarRequest();
        request.setUsuario("usuario_inexistente");
        request.setContrasena("contrasena_incorrecta");
        request.setSql("CREATE DATABASE testdb;\n\\c testdb;\nCREATE TABLE test (id INTEGER);");

        EjecutarResponse response = service.ejecutar(request);

        assertFalse(response.isExito());
        assertNotNull(response.getMensaje());
        assertFalse(response.getMensaje().isEmpty());
    }
}
