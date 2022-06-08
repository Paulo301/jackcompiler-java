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

        expectPeek(CLASS);
        expectPeek(IDENTIFIER);

        expectPeek(LBRACE);
        while(peekTokenIs(STATIC) || peekTokenIs(FIELD)){
            parseClassVarDec();
        }

        while(peekTokenIs(CONSTRUCTOR) || peekTokenIs(FUNCTION) || peekTokenIs(METHOD)){
            parseSubroutineDec();
        }
        expectPeek(RBRACE);

        printNonTerminal("/class");
    }

    void parseSubroutineCall() {
        if(peekTokenIs(LPAREN)){
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        } else {
            expectPeek(DOT);
            expectPeek(IDENTIFIER);

            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        }
    }

    void parseVarDec() {
        printNonTerminal("varDec");

        expectPeek(VAR);
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);

        while(peekTokenIs(COMMA)){
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }

        expectPeek(SEMICOLON);

        printNonTerminal("/varDec");
    }

    void parseClassVarDec() {
        printNonTerminal("classVarDec");
        
        expectPeek(STATIC, FIELD);
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);
        while(peekTokenIs(COMMA)){
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/classVarDec");
    }

    void parseSubroutineDec() {
        printNonTerminal("subroutineDec");
        
        expectPeek(CONSTRUCTOR, FUNCTION, METHOD);
        expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);

        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);

        parseSubroutineBody();

        printNonTerminal("/subroutineDec");
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

        if(peekTokenIs(INT) || peekTokenIs(CHAR) || peekTokenIs(BOOLEAN) || peekTokenIs(IDENTIFIER)){
            expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
            expectPeek(IDENTIFIER);
            while(peekTokenIs(COMMA)){
                expectPeek(COMMA);
                expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
                expectPeek(IDENTIFIER);
            }
        }
   
        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody() {

        printNonTerminal("subroutineBody");

        expectPeek(LBRACE);

        while(peekTokenIs(VAR)){
            parseVarDec();
        }
        parseStatements();

        expectPeek(RBRACE);
     
        printNonTerminal("/subroutineBody");
    }

    //'let' varName ( '[' expression ']' )? '=' expression ';'
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

    //'while' '(' expression ')' '{' statements '}'
    void parseWhile() {
        printNonTerminal("whileStatement");

        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
  
        printNonTerminal("/whileStatement");
    }

    //'do' subroutineCall ';'
    void parseDo() {
        printNonTerminal("doStatement");
        
        expectPeek(DO);
        expectPeek(IDENTIFIER);
        parseSubroutineCall();
        expectPeek(SEMICOLON);

        printNonTerminal("/doStatement");
    }

    //'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
    void parseIf() {
        printNonTerminal("ifStatement");

        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);

        if(peekTokenIs(ELSE)){
            expectPeek(ELSE);
            expectPeek(LBRACE);
            parseStatements();
            expectPeek(RBRACE);
        }

        printNonTerminal("/ifStatement");
    }

    void parseStatements() {
        printNonTerminal("statements");
        
        while(peekTokenIs(LET) || peekTokenIs(IF) || peekTokenIs(WHILE) || peekTokenIs(DO) || peekTokenIs(RETURN)){
            parseStatement();
        }

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

    //'return' expression? ';'

    void parseReturn() {
        printNonTerminal("returnStatement");
        
        expectPeek(RETURN);
        if(!peekTokenIs(SEMICOLON)){
            parseExpression();
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/returnStatement");
    }

    void parseExpressionList() {
        printNonTerminal("expressionList");

        if(!peekTokenIs(RPAREN)){
            parseExpression();
            while(peekTokenIs(COMMA)){
                expectPeek(COMMA);
                parseExpression();
            }
        }

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
                if(peekTokenIs (LPAREN) || peekTokenIs(DOT)){
                    parseSubroutineCall();
                }
                break;
            case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
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