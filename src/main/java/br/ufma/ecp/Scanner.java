package br.ufma.ecp;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
  private byte[] input;
  private int current;
  private int start;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();

    keywords.put("while", TokenType.WHILE);
    keywords.put("class", TokenType.CLASS);
    keywords.put("constructor", TokenType.CONSTRUCTOR);
    keywords.put("function", TokenType.FUNCTION);
    keywords.put("method", TokenType.METHOD);
    keywords.put("field", TokenType.FIELD);
    keywords.put("static", TokenType.STATIC);
    keywords.put("var", TokenType.VAR);
    keywords.put("int", TokenType.INT);
    keywords.put("char", TokenType.CHAR);
    keywords.put("boolean", TokenType.BOOLEAN);
    keywords.put("void", TokenType.VOID);
    keywords.put("true", TokenType.TRUE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("null", TokenType.NULL);
    keywords.put("this", TokenType.THIS);
    keywords.put("let", TokenType.LET);
    keywords.put("do", TokenType.DO);
    keywords.put("if", TokenType.IF);
    keywords.put("else", TokenType.ELSE);
    keywords.put("return", TokenType.RETURN);
  }

  public Scanner(byte[] input){
    this.input = input;
    current = 0;
    start = 0;
  }

  public Token nextToken() {
    skipWhitespace();

    start = current;
    char ch = peek();

    if(Character.isDigit(ch)){
      return number();
    }

    if(Character.isAlphabetic(ch)){
      return identifier();
    }

    switch(ch){
      case '+':
        advance();
        return new Token(TokenType.PLUS, "+");
      case '-':
        advance();
        return new Token(TokenType.MINUS, "-");
      case 0:
        return new Token(TokenType.EOF, "EOF");
      default:
        advance();
        return new Token(TokenType.ILLEGAL, Character.toString(ch));
    }
  }

  private void skipWhitespace(){
    char ch = peek();

    while(ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n'){
      advance();
      ch = peek();
    }
  }

  private boolean isAlphanumeric(char ch){
    return Character.isLetter(ch) || Character.isDigit(ch);
  }

  private Token identifier(){
    while(isAlphanumeric(peek())){
      advance();
    }
    String id = new String(input, start, current-start, StandardCharsets.UTF_8);
    TokenType type = keywords.get(id);
    if(type == null) type = TokenType.IDENTIFIER;
    Token token = new Token(type, id);
    return token;
  }

  private Token number(){
    while(Character.isDigit(peek())){
      advance();
    }
    String s = new String(input, start, current-start, StandardCharsets.UTF_8);
    Token token = new Token(TokenType.NUMBER, s);
    return token;
  }

  private void advance(){
    char ch = peek();

    if(ch != 0){
      current++;
    }
  }

  private char peek(){
    if(current < input.length){
      return (char)input[current];
    } else {
      return 0;
    }
  }
}
