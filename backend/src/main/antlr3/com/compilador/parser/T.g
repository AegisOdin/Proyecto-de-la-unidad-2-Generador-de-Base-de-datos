grammar T;

@header {
package com.compilador.parser;

import java.util.ArrayList;
import java.util.List;
import com.compilador.model.Tabla;
import com.compilador.model.Atributo;
}

@lexer::header {
package com.compilador.parser;

import java.util.ArrayList;
import java.util.List;
}

@members {
    List<Tabla> tablas = new ArrayList<Tabla>();
    Tabla tablaActual = null;
    StringBuilder sqlBuilder = new StringBuilder();
    StringBuilder estructuraBuilder = new StringBuilder();
    List<String> errores = new ArrayList<String>();
    String nombreBaseDatos = "";
    List<String> foreignKeys = new ArrayList<String>();

    public String getSql() { return sqlBuilder.toString(); }
    public String getEstructura() { return estructuraBuilder.toString(); }
    public List<String> getErrores() { return errores; }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errores.add(hdr + " " + msg);
    }
}

@lexer::members {
    List<String> erroresLexer = new ArrayList<String>();

    public List<String> getErroresLexer() { return erroresLexer; }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        erroresLexer.add(hdr + " " + msg);
    }
}

inicio : creacion usar tabla+ cerrar;

creacion : CREAR ID
    {
        nombreBaseDatos = $ID.text;
        sqlBuilder.append("CREATE DATABASE ").append($ID.text).append(";\n");
    };

usar : USAR ID
    {
        sqlBuilder.append("\\c ").append($ID.text).append(";\n\n");
    };

tabla : TABLA nom=ID INICIO
    {
        String tableName = $nom.text;
        sqlBuilder.append("CREATE TABLE ").append(tableName).append(" (\n");
        sqlBuilder.append("  ").append(tableName).append("_key SERIAL PRIMARY KEY");

        Tabla t = new Tabla();
        t.nombre = tableName;
        tablas.add(t);
        tablaActual = t;
        foreignKeys.clear();
    }
    campo+
    FIN
    {
        for (String fk : foreignKeys) {
            sqlBuilder.append(",\n  ").append(fk);
        }
        sqlBuilder.append("\n);\n\n");
    };

campo : n=ID (t=NUMERICO | t=ALFABETICO | t=FECHA | t=ID)
    {
        String fieldName = $n.text;
        String fieldType = $t.text;

        Atributo a = new Atributo();
        a.nombreAtributo = fieldName;
        a.tipoAtributo = fieldType;

        if ($t.getType() == ID) {
            // Llave foranea: referencia a otra tabla
            sqlBuilder.append(",\n  ").append(fieldName).append(" INTEGER");
            foreignKeys.add("FOREIGN KEY (" + fieldName + ") REFERENCES " + fieldType + "(" + fieldType + "_key)");
            a.esForeignKey = true;
            a.tablaReferencia = fieldType;
        } else if (fieldType.equals("letras")) {
            sqlBuilder.append(",\n  ").append(fieldName).append(" VARCHAR(300)");
        } else if (fieldType.equals("fecha")) {
            sqlBuilder.append(",\n  ").append(fieldName).append(" DATE");
        } else if (fieldType.equals("numeros")) {
            sqlBuilder.append(",\n  ").append(fieldName).append(" INTEGER");
        }

        tablaActual.atributos.add(a);
    };

cerrar : CERRAR
    {
        estructuraBuilder.append("Base de datos: ").append(nombreBaseDatos).append("\n");
        estructuraBuilder.append("========================================\n\n");
        for (int i = 0; i < tablas.size(); i++) {
            Tabla t = tablas.get(i);
            estructuraBuilder.append("Tabla: ").append(t.nombre).append("\n");
            estructuraBuilder.append("  ").append(t.nombre).append("_key\tSERIAL (PK)\n");
            for (int j = 0; j < t.atributos.size(); j++) {
                Atributo at = t.atributos.get(j);
                estructuraBuilder.append("  ").append(at.nombreAtributo).append("\t").append(at.tipoAtributo);
                if (at.esForeignKey) {
                    estructuraBuilder.append(" (FK -> ").append(at.tablaReferencia).append(")");
                }
                estructuraBuilder.append("\n");
            }
            estructuraBuilder.append("\n");
        }
    };

CERRAR    : 'cerrar';
NUMERICO  : 'numeros';
ALFABETICO: 'letras';
FECHA     : 'fecha';
TABLA     : 'tabla';
INICIO    : 'inicio';
FIN       : 'fin';
USAR      : 'usar';
CREAR     : 'crear';
ID        : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
WS        : (' ' | '\n' | '\t' | '\r')+ { $channel=HIDDEN; };
