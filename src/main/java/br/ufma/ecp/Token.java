package br.ufma.ecp;

import java.util.Arrays;
import java.util.List;

public class Token {
  final TokenType type;
  final String lexeme;

  List<String> symbols = Arrays.asList("{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~");
  List<String> keywords = Arrays.asList("while", "class", "constructor", "function", "method", "field", "static", "var", "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "return");
  
  public Token(TokenType type, String lexeme){
    this.type = type;
    this.lexeme = lexeme;
  }

  public String toString(){
    String categoria = type.toString();
    String representante = lexeme;
    
    if(symbols.contains(lexeme)){
      if(lexeme == "<"){
        representante = "&lt";
      } else if(lexeme == ">"){
        representante = "&gt";
      } else if(lexeme == "\""){
        representante = "&quot";
      } else if(lexeme == "&"){
        representante = "&amp";
      }
      categoria = "symbol";
    } else if(keywords.contains(lexeme)){
      categoria = "keyword";
    } else if(categoria == "NUMBER"){
      categoria = "integerConstant";
    }

    return "<" + categoria + ">" + representante + "</" + categoria + ">";
  }
}
