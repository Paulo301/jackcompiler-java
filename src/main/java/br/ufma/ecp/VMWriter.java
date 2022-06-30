package br.ufma.ecp;

public class VMWriter {
  private StringBuilder vmOutput = new StringBuilder();

  enum Segment {
    CONST("constant"),
    ARG("argument"),
    LOCAL("local"),
    STATIC("static"),
    THIS("this"),
    THAT("that"),
    POINTER("pointer"),
    TEMP("temp");

    public String value;

    private Segment(String value) {
      this.value = value;
    }
  }

  enum Command {
    ADD, 
    SUB,
    NEG,
    EQ,
    GT,
    LT,
    AND,
    OR,
    NOT;
  }

  private void writePush(Segment segment, int index){

  }  // Writes a VM push command
  private void writePop(Segment segment, int index){

  }   // Writes a VM pop command
  private void writeArithmetic(Command command){

  }       // Writes a VM arithmetic command
  private void writeLabel(String label){

  }               // Writes a VM label comand
  private void writeGoto(String label){

  }                // Writes a VM goto command
  private void writeIf(String label){

  }                  // Writes a VM if-goto command
  private void writeCall(String name, int nArgs){

  }      // Writes a VM call command
  private void writeFunction(String name, int nLocals){

  } // Writes a VM function command
  private void writeReturn(){

  }
}
