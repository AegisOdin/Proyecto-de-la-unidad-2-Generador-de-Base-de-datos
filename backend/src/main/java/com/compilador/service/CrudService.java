package com.compilador.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrudService {

    @Value("${crud.db.host}") private String dbHost;
    @Value("${crud.db.port}") private String dbPort;
    @Value("${crud.db.user}") private String dbUser;
    @Value("${crud.db.password}") private String dbPassword;

    private static final java.util.regex.Pattern IDENT = java.util.regex.Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]{0,62}");

    private Connection conn(String baseDatos) throws SQLException {
        if (!IDENT.matcher(baseDatos).matches()) throw new SQLException("BD invalida");
        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + baseDatos;
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }

    private void validateIdent(String s) throws SQLException {
        if (!IDENT.matcher(s).matches()) throw new SQLException("Identificador invalido: " + s);
    }

    public List<Map<String, Object>> listar(String baseDatos, String tabla) throws SQLException {
        validateIdent(tabla);
        try (Connection c = conn(baseDatos);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tabla + " ORDER BY " + tabla + "_key")) {
            return rowsToList(rs);
        }
    }

    public Map<String, Object> insertar(String baseDatos, String tabla, Map<String, Object> data) throws SQLException {
        validateIdent(tabla);
        if (data == null || data.isEmpty()) throw new SQLException("Sin datos");
        StringBuilder cols = new StringBuilder();
        StringBuilder ph = new StringBuilder();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            validateIdent(e.getKey());
            if (cols.length() > 0) { cols.append(", "); ph.append(", "); }
            cols.append(e.getKey()); ph.append("?");
            vals.add(e.getValue());
        }
        String sql = "INSERT INTO " + tabla + " (" + cols + ") VALUES (" + ph + ") RETURNING " + tabla + "_key";
        try (Connection c = conn(baseDatos);
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++) setParam(ps, i + 1, vals.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Object> out = new LinkedHashMap<>();
                if (rs.next()) out.put("id", rs.getObject(1));
                out.put("exito", true);
                return out;
            }
        }
    }

    public Map<String, Object> actualizar(String baseDatos, String tabla, Object id, Map<String, Object> data) throws SQLException {
        validateIdent(tabla);
        if (data == null || data.isEmpty()) throw new SQLException("Sin datos");
        StringBuilder set = new StringBuilder();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            validateIdent(e.getKey());
            if (set.length() > 0) set.append(", ");
            set.append(e.getKey()).append(" = ?");
            vals.add(e.getValue());
        }
        String sql = "UPDATE " + tabla + " SET " + set + " WHERE " + tabla + "_key = ?";
        try (Connection c = conn(baseDatos);
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++) setParam(ps, i + 1, vals.get(i));
            setParam(ps, vals.size() + 1, id);
            ps.executeUpdate();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("exito", true);
            return out;
        }
    }

    public Map<String, Object> borrar(String baseDatos, String tabla, Object id) throws SQLException {
        validateIdent(tabla);
        try (Connection c = conn(baseDatos);
             PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla + " WHERE " + tabla + "_key = ?")) {
            ps.setObject(1, id);
            ps.executeUpdate();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("exito", true);
            return out;
        }
    }

    private void setParam(PreparedStatement ps, int idx, Object val) throws SQLException {
        if (val == null) {
            ps.setNull(idx, java.sql.Types.OTHER);
        } else if (val instanceof String) {
            // Forzar a postgres a castear string al tipo de la columna (INTEGER, DATE, etc.)
            ps.setObject(idx, val, java.sql.Types.OTHER);
        } else {
            ps.setObject(idx, val);
        }
    }

    private List<Map<String, Object>> rowsToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) row.put(md.getColumnLabel(i), rs.getObject(i));
            list.add(row);
        }
        return list;
    }
}
