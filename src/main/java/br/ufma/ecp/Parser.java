package br.ufma.ecp;

public class Parser {

  private Scanner scan;
  private Token currentToken;
  private Token peekToken;

  public Parser(byte[] input){
    this.scan = new Scanner(input);
    nextToken();
  }

  private void nextToken(){
    this.currentToken = peekToken;
    this.peekToken = scan.nextToken();
  }

  // private void match(TokenType type){
  //   if(currentToken.type == type){
  //     nextToken();
  //   } else {
  //     throw new Error("Syntax error - expected " + type + " found "+ currentToken.lexeme);
  //   }
  // }

  void parserLet(){
    System.out.println("<letStatement>");
    expectPeek(TokenType.LET);
    expectPeek(TokenType.IDENTIFIER);
    expectPeek(TokenType.ASSIGN);
    parserExpression();
    expectPeek(TokenType.SEMICOLON);
    System.out.println("</letStatement>");
  }

  void parserExpression() {
    System.out.println("<expression>");
    parserTerm();
    while(peekTokenIs(TokenType.PLUS)){
      expectPeek(TokenType.PLUS);
      parserTerm();
    }
    System.out.println("</expression>");
  }

  void parserTerm(){
    System.out.println("<term>");
    switch(peekToken.type){
      case NUMBER:
        expectPeek(TokenType.NUMBER);
        break;
      case IDENTIFIER:
        expectPeek(TokenType.IDENTIFIER);
        break;
      default:
        ;
    }
    System.out.println("</term>");
  }

  void parser(){
    parserLet();
  }

  boolean peekTokenIs(TokenType type){
    return peekToken.type == type;
  }

  boolean currentTokenIs(TokenType type){
    return currentToken.type == type;
  }

  private void expectPeek(TokenType type){
    if(peekToken.type == type){
      nextToken();
      System.out.println((currentToken));
    } else {
      throw new Error("Syntax error - expected " + type + " found "+ peekToken.lexeme);
    }
  }

  // void expr(){
  //   term();
  //   oper();
  // }

  // void term(){
  //   if(currentTokenIs(TokenType.NUMBER)){
  //     number();
  //   } else if(currentTokenIs(TokenType.IDENTIFIER)){
  //     identifier();
  //   } else {
  //     throw new Error("Syntax error found "+currentToken.lexeme);
  //   }
  // }

  // void number(){
  //   System.out.println("push " + currentToken.lexeme);
  //   match(TokenType.NUMBER);
  // }

  // void identifier(){
  //   System.out.println("push " + currentToken.lexeme);
  //   match(TokenType.IDENTIFIER);
  // }

  // void oper(){
  //   if(currentTokenIs(TokenType.PLUS)){
  //     match(TokenType.PLUS);
  //     term();
  //     System.out.println("add");
  //     oper();
  //   } else if(currentTokenIs(TokenType.MINUS)){
  //     match(TokenType.MINUS);
  //     term();
  //     System.out.println("sub");
  //     oper();
  //   } else if(currentTokenIs(TokenType.EOF)){
  //     //nada
  //   } else {
  //     throw new Error("Syntax error found "+currentToken.lexeme);
  //   }
  // }
}
