// $ANTLR 3.5.3 com\\compilador\\parser\\T.g 2026-04-08 19:41:16

package com.compilador.parser;

import java.util.ArrayList;
import java.util.List;
import com.compilador.model.Tabla;
import com.compilador.model.Atributo;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class TParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALFABETICO", "CERRAR", "CREAR", 
		"FECHA", "FIN", "ID", "INICIO", "NUMERICO", "TABLA", "USAR", "WS"
	};
	public static final int EOF=-1;
	public static final int ALFABETICO=4;
	public static final int CERRAR=5;
	public static final int CREAR=6;
	public static final int FECHA=7;
	public static final int FIN=8;
	public static final int ID=9;
	public static final int INICIO=10;
	public static final int NUMERICO=11;
	public static final int TABLA=12;
	public static final int USAR=13;
	public static final int WS=14;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public TParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public TParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return TParser.tokenNames; }
	@Override public String getGrammarFileName() { return "com\\compilador\\parser\\T.g"; }


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



	// $ANTLR start "inicio"
	// com\\compilador\\parser\\T.g:53:1: inicio : creacion usar ( tabla )+ cerrar ;
	public final void inicio() throws RecognitionException {
		try {
			// com\\compilador\\parser\\T.g:53:8: ( creacion usar ( tabla )+ cerrar )
			// com\\compilador\\parser\\T.g:53:10: creacion usar ( tabla )+ cerrar
			{
			pushFollow(FOLLOW_creacion_in_inicio40);
			creacion();
			state._fsp--;

			pushFollow(FOLLOW_usar_in_inicio42);
			usar();
			state._fsp--;

			// com\\compilador\\parser\\T.g:53:24: ( tabla )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==TABLA) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// com\\compilador\\parser\\T.g:53:24: tabla
					{
					pushFollow(FOLLOW_tabla_in_inicio44);
					tabla();
					state._fsp--;

					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			pushFollow(FOLLOW_cerrar_in_inicio47);
			cerrar();
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "inicio"



	// $ANTLR start "creacion"
	// com\\compilador\\parser\\T.g:55:1: creacion : CREAR ID ;
	public final void creacion() throws RecognitionException {
		Token ID1=null;

		try {
			// com\\compilador\\parser\\T.g:55:10: ( CREAR ID )
			// com\\compilador\\parser\\T.g:55:12: CREAR ID
			{
			match(input,CREAR,FOLLOW_CREAR_in_creacion55); 
			ID1=(Token)match(input,ID,FOLLOW_ID_in_creacion57); 

			        nombreBaseDatos = (ID1!=null?ID1.getText():null);
			        sqlBuilder.append("CREATE DATABASE ").append((ID1!=null?ID1.getText():null)).append(";\n");
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "creacion"



	// $ANTLR start "usar"
	// com\\compilador\\parser\\T.g:61:1: usar : USAR ID ;
	public final void usar() throws RecognitionException {
		Token ID2=null;

		try {
			// com\\compilador\\parser\\T.g:61:6: ( USAR ID )
			// com\\compilador\\parser\\T.g:61:8: USAR ID
			{
			match(input,USAR,FOLLOW_USAR_in_usar71); 
			ID2=(Token)match(input,ID,FOLLOW_ID_in_usar73); 

			        sqlBuilder.append("\\c ").append((ID2!=null?ID2.getText():null)).append(";\n\n");
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "usar"



	// $ANTLR start "tabla"
	// com\\compilador\\parser\\T.g:66:1: tabla : TABLA nom= ID INICIO ( campo )+ FIN ;
	public final void tabla() throws RecognitionException {
		Token nom=null;

		try {
			// com\\compilador\\parser\\T.g:66:7: ( TABLA nom= ID INICIO ( campo )+ FIN )
			// com\\compilador\\parser\\T.g:66:9: TABLA nom= ID INICIO ( campo )+ FIN
			{
			match(input,TABLA,FOLLOW_TABLA_in_tabla87); 
			nom=(Token)match(input,ID,FOLLOW_ID_in_tabla91); 
			match(input,INICIO,FOLLOW_INICIO_in_tabla93); 

			        String tableName = (nom!=null?nom.getText():null);
			        sqlBuilder.append("CREATE TABLE ").append(tableName).append(" (\n");
			        sqlBuilder.append("  ").append(tableName).append("_key SERIAL PRIMARY KEY");

			        Tabla t = new Tabla();
			        t.nombre = tableName;
			        tablas.add(t);
			        tablaActual = t;
			        foreignKeys.clear();
			    
			// com\\compilador\\parser\\T.g:78:5: ( campo )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==ID) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// com\\compilador\\parser\\T.g:78:5: campo
					{
					pushFollow(FOLLOW_campo_in_tabla105);
					campo();
					state._fsp--;

					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			match(input,FIN,FOLLOW_FIN_in_tabla112); 

			        for (String fk : foreignKeys) {
			            sqlBuilder.append(",\n  ").append(fk);
			        }
			        sqlBuilder.append("\n);\n\n");
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "tabla"



	// $ANTLR start "campo"
	// com\\compilador\\parser\\T.g:87:1: campo : n= ID (t= NUMERICO |t= ALFABETICO |t= FECHA |t= ID ) ;
	public final void campo() throws RecognitionException {
		Token n=null;
		Token t=null;

		try {
			// com\\compilador\\parser\\T.g:87:7: (n= ID (t= NUMERICO |t= ALFABETICO |t= FECHA |t= ID ) )
			// com\\compilador\\parser\\T.g:87:9: n= ID (t= NUMERICO |t= ALFABETICO |t= FECHA |t= ID )
			{
			n=(Token)match(input,ID,FOLLOW_ID_in_campo128); 
			// com\\compilador\\parser\\T.g:87:14: (t= NUMERICO |t= ALFABETICO |t= FECHA |t= ID )
			int alt3=4;
			switch ( input.LA(1) ) {
			case NUMERICO:
				{
				alt3=1;
				}
				break;
			case ALFABETICO:
				{
				alt3=2;
				}
				break;
			case FECHA:
				{
				alt3=3;
				}
				break;
			case ID:
				{
				alt3=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}
			switch (alt3) {
				case 1 :
					// com\\compilador\\parser\\T.g:87:15: t= NUMERICO
					{
					t=(Token)match(input,NUMERICO,FOLLOW_NUMERICO_in_campo133); 
					}
					break;
				case 2 :
					// com\\compilador\\parser\\T.g:87:28: t= ALFABETICO
					{
					t=(Token)match(input,ALFABETICO,FOLLOW_ALFABETICO_in_campo139); 
					}
					break;
				case 3 :
					// com\\compilador\\parser\\T.g:87:43: t= FECHA
					{
					t=(Token)match(input,FECHA,FOLLOW_FECHA_in_campo145); 
					}
					break;
				case 4 :
					// com\\compilador\\parser\\T.g:87:53: t= ID
					{
					t=(Token)match(input,ID,FOLLOW_ID_in_campo151); 
					}
					break;

			}


			        String fieldName = (n!=null?n.getText():null);
			        String fieldType = (t!=null?t.getText():null);

			        Atributo a = new Atributo();
			        a.nombreAtributo = fieldName;
			        a.tipoAtributo = fieldType;

			        if (t.getType() == ID) {
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
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "campo"



	// $ANTLR start "cerrar"
	// com\\compilador\\parser\\T.g:113:1: cerrar : CERRAR ;
	public final void cerrar() throws RecognitionException {
		try {
			// com\\compilador\\parser\\T.g:113:8: ( CERRAR )
			// com\\compilador\\parser\\T.g:113:10: CERRAR
			{
			match(input,CERRAR,FOLLOW_CERRAR_in_cerrar166); 

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
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cerrar"

	// Delegated rules



	public static final BitSet FOLLOW_creacion_in_inicio40 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_usar_in_inicio42 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_tabla_in_inicio44 = new BitSet(new long[]{0x0000000000001020L});
	public static final BitSet FOLLOW_cerrar_in_inicio47 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CREAR_in_creacion55 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_creacion57 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_USAR_in_usar71 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_usar73 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TABLA_in_tabla87 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ID_in_tabla91 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_INICIO_in_tabla93 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_campo_in_tabla105 = new BitSet(new long[]{0x0000000000000300L});
	public static final BitSet FOLLOW_FIN_in_tabla112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_campo128 = new BitSet(new long[]{0x0000000000000A90L});
	public static final BitSet FOLLOW_NUMERICO_in_campo133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ALFABETICO_in_campo139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FECHA_in_campo145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_campo151 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CERRAR_in_cerrar166 = new BitSet(new long[]{0x0000000000000002L});
}
