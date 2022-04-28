package br.ufma.ecp;

public class Parser {
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
