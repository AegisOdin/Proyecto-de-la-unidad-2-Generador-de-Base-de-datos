package com.compilador.service;

import com.compilador.dto.CompiladorResponse;
import com.compilador.parser.TLexer;
import com.compilador.parser.TParser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompiladorService {

    public CompiladorResponse compilar(String codigo) {
        CompiladorResponse response = new CompiladorResponse();
        List<String> errores = new ArrayList<>();

        try {
            ANTLRStringStream input = new ANTLRStringStream(codigo);
            TLexer lexer = new TLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            TParser parser = new TParser(tokens);

            parser.inicio();

            // Recolectar errores del lexer y del parser
            errores.addAll(lexer.getErroresLexer());
            errores.addAll(parser.getErrores());

            if (errores.isEmpty()) {
                response.setSql(parser.getSql());
                response.setEstructura(parser.getEstructura());
            } else {
                response.setSql("");
                response.setEstructura("");
            }
        } catch (Exception e) {
            errores.add("Error interno del compilador: " + e.getMessage());
            response.setSql("");
            response.setEstructura("");
        }

        response.setErrores(errores);
        return response;
    }
}
