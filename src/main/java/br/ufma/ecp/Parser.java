package br.ufma.ecp;

public class Parser {

  private byte[] input;
  private int current;

  private Parser(byte[] input){
    this.input = input;
  }

  private void match(char c){
    if(c == peek()){
      current++;
    } else {
      throw new Error("Syntax error");
    }
  }

  private char peek(){
    if(current < input.length){
      return (char)input[current];
    } else {
      return 0;
    }
  }

  void digit(){

  }

  void expr(){
    expr();
    match('+');
    digit();
  }
}
