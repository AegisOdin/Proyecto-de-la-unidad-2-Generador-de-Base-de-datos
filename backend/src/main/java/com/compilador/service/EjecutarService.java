package com.compilador.service;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class EjecutarService {

    public EjecutarResponse ejecutar(EjecutarRequest request) {
        EjecutarResponse response = new EjecutarResponse();
        String usuario = request.getUsuario();
        String contrasena = request.getContrasena();
        String baseDatos = request.getBaseDatos();
        String sql = request.getSql();

        // Paso 1: Crear la BD conectándose a la BD de mantenimiento 'postgres'
        String urlPostgres = "jdbc:postgresql://localhost:5432/postgres";
        try (Connection conn = DriverManager.getConnection(urlPostgres, usuario, contrasena);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE " + baseDatos);
        } catch (SQLException e) {
            // Si la BD ya existe, no es un error — continuamos
            if (!e.getMessage().contains("already exists")) {
                response.setExito(false);
                response.setMensaje(e.getMessage());
                return response;
            }
        }

        // Paso 2: Conectarse a la BD y ejecutar los CREATE TABLE
        // El SQL del parser incluye "CREATE DATABASE x;" y "\c x;" que hay que ignorar
        String urlBd = "jdbc:postgresql://localhost:5432/" + baseDatos;
        try (Connection conn = DriverManager.getConnection(urlBd, usuario, contrasena);
             Statement stmt = conn.createStatement()) {

            for (String sentencia : sql.split(";")) {
                String trimmed = sentencia.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("\\")) continue;
                if (trimmed.toUpperCase().startsWith("CREATE DATABASE")) continue;
                stmt.execute(trimmed);
            }

            response.setExito(true);
            response.setMensaje("Base de datos creada exitosamente");
        } catch (SQLException e) {
            response.setExito(false);
            response.setMensaje(e.getMessage());
        }

        return response;
    }
}
