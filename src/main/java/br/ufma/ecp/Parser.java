package br.ufma.ecp;

public class Parser {

  private Scanner scan;
  private Token currentToken;

  public Parser(byte[] input){
    this.scan = new Scanner(input);
    nextToken();
  }

  private void nextToken(){
    this.currentToken = scan.nextToken();
  }

  private void match(TokenType type){
    if(currentToken.type == type){
      nextToken();
    } else {
      throw new Error("Syntax error - expected " + type + " found "+ currentToken.lexeme);
    }
  }

  void parser(){
    expr();
  }

  void expr(){
    term();
    oper();
  }

  void term(){
    if(currentTokenIs(TokenType.NUMBER)){
      number();
    } else if(currentTokenIs(TokenType.IDENTIFIER)){
      identifier();
    } else {
      throw new Error("Syntax error found "+currentToken.lexeme);
    }
  }

  void number(){
    System.out.println("push " + currentToken.lexeme);
    match(TokenType.NUMBER);
  }

  void identifier(){
    System.out.println("push " + currentToken.lexeme);
    match(TokenType.IDENTIFIER);
  }

  void oper(){
    if(currentTokenIs(TokenType.PLUS)){
      match(TokenType.PLUS);
      term();
      System.out.println("add");
      oper();
    } else if(currentTokenIs(TokenType.MINUS)){
      match(TokenType.MINUS);
      term();
      System.out.println("sub");
      oper();
    } else if(currentTokenIs(TokenType.EOF)){
      //nada
    } else {
      throw new Error("Syntax error found "+currentToken.lexeme);
    }
  }

  boolean currentTokenIs(TokenType type){
    return currentToken.type == type;
  }
}
