package com.compilador.model;

import java.util.ArrayList;
import java.util.List;

public class Tabla {
    public String nombre;
    public List<Atributo> atributos;

    public Tabla() {
        atributos = new ArrayList<>();
    }
}
