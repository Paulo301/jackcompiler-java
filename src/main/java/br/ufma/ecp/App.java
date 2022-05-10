package br.ufma.ecp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class App 
{

    private static String fromFile() {
        File file = new File("Main.jack");

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file.toPath());
            String textoDoArquivo = new String(bytes, "UTF-8");
            return textoDoArquivo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    } 

    public static void main( String[] args )
    {
        // String input = "58 +peixe78+ 6-8";
        // Parser p = new Parser(input.getBytes());
        // p.parser();

        // String input = "58+ 6-8";
        Scanner scan = new Scanner(fromFile().getBytes());
        for(Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()){
            System.out.println(tk);
        }
    }
}
