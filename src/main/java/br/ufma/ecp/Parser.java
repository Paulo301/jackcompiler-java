package br.ufma.ecp;

public class Parser {

  private byte[] input;
  private int current;

  private Parser(byte[] input){
    this.input = input;
  }

  void digit(){

  }

  void expr(){
    expr();
    match('+');
    digit();
  }

  void match(char c){

  }
}
