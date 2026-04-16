package com.compilador.controller;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import com.compilador.service.EjecutarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EjecutarController.class)
class EjecutarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EjecutarService ejecutarService;

    @Test
    void ejecutar_retornaExitoVerdadero_cuandoElServicioTieneExito() throws Exception {
        EjecutarResponse respuesta = new EjecutarResponse();
        respuesta.setExito(true);
        respuesta.setMensaje("Base de datos creada exitosamente");

        when(ejecutarService.ejecutar(any(EjecutarRequest.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/ejecutar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"postgres\",\"contrasena\":\"1234\",\"sql\":\"CREATE DATABASE empresa;\\n\\\\c empresa;\\nCREATE TABLE test (id INTEGER);\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.mensaje").value("Base de datos creada exitosamente"));
    }
}
