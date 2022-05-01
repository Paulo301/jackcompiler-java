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
      throw new Error("Syntax error");
    }
  }

  void parser(){
    expr();
  }

  void expr(){
    number();
    oper();
  }

  void number(){
    System.out.println("push " + currentToken.lexeme);
    match(TokenType.NUMBER);
  }

  void oper(){
    if(currentTokenIs(TokenType.PLUS)){
      match(TokenType.PLUS);
      number();
      System.out.println("add");
      oper();
    } else if(currentTokenIs(TokenType.MINUS)){
      match(TokenType.MINUS);
      number();
      System.out.println("sub");
      oper();
    } else if(currentTokenIs(TokenType.EOF)){
      //nada
    } else {
      throw new Error("Syntax error");
    }
  }

  boolean currentTokenIs(TokenType type){
    return currentToken.type == type;
  }
}
