package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;


import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {


    private static class ParseError extends RuntimeException {}

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();

    public Parser(byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

    void parse() {
        parseClass();
    }

    void parseClass() {
        printNonTerminal("class");
   

        printNonTerminal("/class");
    }

    void parseSubroutineCall() {
        
    }

    void parseDo() {
        printNonTerminal("doStatement");
    

        printNonTerminal("/doStatement");
    }

    void parseVarDec() {
        printNonTerminal("varDec");

        printNonTerminal("/varDec");
    }

    void parseClassVarDec() {
        printNonTerminal("classVarDec");

        printNonTerminal("/classVarDec");
    }

    void parseSubroutineDec() {
        printNonTerminal("subroutineDec");
   

        printNonTerminal("/subroutineDec");
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

   
        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody() {

        printNonTerminal("subroutineBody");
     
        printNonTerminal("/subroutineBody");
    }

    void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(LET);
        expectPeek(IDENTIFIER);

        if (peekTokenIs(LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();
            expectPeek(RBRACKET);
        }

        expectPeek(EQ);
        parseExpression();
        expectPeek(SEMICOLON);
        printNonTerminal("/letStatement");
    }

    void parseWhile() {
        printNonTerminal("whileStatement");
  
        printNonTerminal("/whileStatement");
    }

    void parseIf() {
        printNonTerminal("ifStatement");

        printNonTerminal("/ifStatement");
    }

    void parseStatements() {
        printNonTerminal("statements");

        printNonTerminal("/statements");
    }

    void parseStatement() {
        switch(peekToken.type){
            case LET:
                parseLet();
                break;
            case WHILE:
                parseWhile();
                break;
            case IF:
                parseIf();
                break;
            case RETURN:
                parseReturn();
                break;
            case DO:
                parseDo();
                break;
            default:
                throw error (peekToken, "Expected a statement");
        }
    }

    void parseReturn() {
        printNonTerminal("returnStatement");
        

        printNonTerminal("/returnStatement");
    }

    void parseExpressionList() {
        printNonTerminal("expressionList");

  
        printNonTerminal("/expressionList");
    }

    void parseExpression() {
        printNonTerminal("expression");
        parseTerm();
        while (isOperator(peekToken.type)) {
            expectPeek(peekToken.type);
            parseTerm();
        }
        printNonTerminal("/expression");
    }


    void parseTerm() {
        printNonTerminal("term");
        switch (peekToken.type) {
            case INTEGER:
                expectPeek(INTEGER);
                break;
            case STRING:
                expectPeek(STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
            case THIS:
                expectPeek(FALSE, NULL, TRUE, THIS);
                break;
            case IDENTIFIER:
                expectPeek(IDENTIFIER);
                if (peekTokenIs (LBRACKET) ) {
                    expectPeek(LBRACKET);
                    parseExpression();
                    expectPeek(RBRACKET);
                }
                break;
            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                parseTerm();
                break;
            default:
                throw error (peekToken, "term expected");
        }
        printNonTerminal("/term");
    }

    // funções auxiliares
    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }


    boolean peekTokenIs(TokenType type) {
        return peekToken.type == type;
    }

    boolean currentTokenIs(TokenType type) {
        return currentToken.type == type;
    }

    private void expectPeek(TokenType... types) {
        for (TokenType type : types) {
            if (peekToken.type == type) {
                expectPeek(type);
                return;
            }
        }

       throw error(peekToken, "Expected a statement");

    }

    private void expectPeek(TokenType type) {
        if (peekToken.type == type) {
            nextToken();
            xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
        } else {
            throw error(peekToken, "Expected "+type.name());
        }
    }


    private static void report(int line, String where,
        String message) {
            System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
    }


    private ParseError error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.value() + "'", message);
        }
        return new ParseError();
    }

}