// Generated from c:/Users/oding/OneDrive/Documents/Proyecto unidad 2/Proyecto-de-la-unidad-2-Generador-de-Base-de-datos/backend/src/main/antlr3/com/compilador/parser/T.g by ANTLR 4.13.1

package com.compilador.parser;

import java.util.ArrayList;
import java.util.List;
import com.compilador.model.Tabla;
import com.compilador.model.Atributo;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TParser}.
 */
public interface TListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TParser#inicio}.
	 * @param ctx the parse tree
	 */
	void enterInicio(TParser.InicioContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#inicio}.
	 * @param ctx the parse tree
	 */
	void exitInicio(TParser.InicioContext ctx);
	/**
	 * Enter a parse tree produced by {@link TParser#creacion}.
	 * @param ctx the parse tree
	 */
	void enterCreacion(TParser.CreacionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#creacion}.
	 * @param ctx the parse tree
	 */
	void exitCreacion(TParser.CreacionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TParser#usar}.
	 * @param ctx the parse tree
	 */
	void enterUsar(TParser.UsarContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#usar}.
	 * @param ctx the parse tree
	 */
	void exitUsar(TParser.UsarContext ctx);
	/**
	 * Enter a parse tree produced by {@link TParser#tabla}.
	 * @param ctx the parse tree
	 */
	void enterTabla(TParser.TablaContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#tabla}.
	 * @param ctx the parse tree
	 */
	void exitTabla(TParser.TablaContext ctx);
	/**
	 * Enter a parse tree produced by {@link TParser#campo}.
	 * @param ctx the parse tree
	 */
	void enterCampo(TParser.CampoContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#campo}.
	 * @param ctx the parse tree
	 */
	void exitCampo(TParser.CampoContext ctx);
	/**
	 * Enter a parse tree produced by {@link TParser#cerrar}.
	 * @param ctx the parse tree
	 */
	void enterCerrar(TParser.CerrarContext ctx);
	/**
	 * Exit a parse tree produced by {@link TParser#cerrar}.
	 * @param ctx the parse tree
	 */
	void exitCerrar(TParser.CerrarContext ctx);
}