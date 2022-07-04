package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;

import br.ufma.ecp.SymbolTable.Kind;
import br.ufma.ecp.SymbolTable.Symbol;
import br.ufma.ecp.VMWriter.Command;
import br.ufma.ecp.VMWriter.Segment;
import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {


    private static class ParseError extends RuntimeException {}

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();
    private VMWriter vmWriter = new VMWriter();
    private SymbolTable symbolTable = new SymbolTable();
    private int ifCounter = 0, whileCounter = 0; 

    private String className;

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
        className = currentToken.value();
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
        var kind = Kind.VAR;

        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        var type = currentToken.value();

        expectPeek(IDENTIFIER);
        var name = currentToken.value();

        symbolTable.define(name, type, kind);

        while(peekTokenIs(COMMA)){
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
            name = currentToken.value();

            symbolTable.define(name, type, kind);
        }

        expectPeek(SEMICOLON);

        printNonTerminal("/varDec");
    }

    void parseClassVarDec() {
        printNonTerminal("classVarDec");
        
        expectPeek(STATIC, FIELD);
        Kind kind = Kind.STATIC;
        if(currentTokenIs(FIELD)){
            kind = Kind.FIELD;
        }

        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        String type = currentToken.value();

        expectPeek(IDENTIFIER);
        String name = currentToken.value();

        symbolTable.define(name, type, kind);

        while(peekTokenIs(COMMA)){
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
            name = currentToken.value();

            symbolTable.define(name, type, kind);
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/classVarDec");
    }

    void parseSubroutineDec() {
        symbolTable.startSubroutine();

        printNonTerminal("subroutineDec");

        if(peekTokenIs(CONSTRUCTOR)){
            expectPeek(CONSTRUCTOR);

            vmWriter.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if(peekTokenIs(METHOD)){
            symbolTable.define("this", className, Kind.ARG);
            expectPeek(METHOD);

            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        } else {
            expectPeek(FUNCTION);
        }

        expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);
        var functionName = className + "." + currentToken.value();

        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);

        parseSubroutineBody(functionName);

        printNonTerminal("/subroutineDec");
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

        Kind kind = Kind.ARG;

        if(peekTokenIs(INT) || peekTokenIs(CHAR) || peekTokenIs(BOOLEAN) || peekTokenIs(IDENTIFIER)){
            expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
            String type = currentToken.value();

            expectPeek(IDENTIFIER);
            String name = currentToken.value();

            symbolTable.define(name, type, kind);

            while(peekTokenIs(COMMA)){
                expectPeek(COMMA);
                expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
                type = currentToken.value();

                expectPeek(IDENTIFIER);
                name = currentToken.value();

                symbolTable.define(name, type, kind);
            }
        }
   
        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody(String functionName) {

        printNonTerminal("subroutineBody");

        expectPeek(LBRACE);

        while(peekTokenIs(VAR)){
            parseVarDec();
        }

        var nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        parseStatements();

        expectPeek(RBRACE);
     
        printNonTerminal("/subroutineBody");
    }

    //'let' varName ( '[' expression ']' )? '=' expression ';'
    void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(LET);
        expectPeek(IDENTIFIER);

        var symbol = symbolTable.resolve(currentToken.value());

        if (peekTokenIs(LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();

            vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
            vmWriter.writeArithmetic(Command.ADD);

            expectPeek(RBRACKET);

            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePush(Segment.THAT, 0);
        }

        expectPeek(EQ);
        parseExpression();
        
        //tratar o caso de array
        vmWriter.writePop(kind2Segment(symbol.kind()), symbol.index());

        expectPeek(SEMICOLON);
        printNonTerminal("/letStatement");
    }

    //'while' '(' expression ')' '{' statements '}'
    void parseWhile() {
        String labelExp = "WHILE_EXP" + whileCounter;
        String labelEnd = "WHILE_END" + whileCounter;
        
        printNonTerminal("whileStatement");

        vmWriter.writeLabel(labelExp);
        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        vmWriter.writeIf(labelEnd);

        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);

        vmWriter.writeGoto(labelExp);
        vmWriter.writeLabel(labelEnd);
  
        printNonTerminal("/whileStatement");

        whileCounter += 1;
    }

    //'do' subroutineCall ';'
    void parseDo() {
        printNonTerminal("doStatement");
        
        expectPeek(DO);
        expectPeek(IDENTIFIER);
        parseSubroutineCall();

        vmWriter.writePop(Segment.TEMP, 0);
        expectPeek(SEMICOLON);

        printNonTerminal("/doStatement");
    }

    //'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
    void parseIf() {
        printNonTerminal("ifStatement");

        String labelTrue = "IF_TRUE" + ifCounter;
        String labelFalse = "IF_FALSE" + ifCounter;
        String labelEnd = "IF_END" + ifCounter;

        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        vmWriter.writeIf(labelTrue);
        vmWriter.writeGoto(labelFalse);

        vmWriter.writeLabel(labelTrue);
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
        vmWriter.writeGoto(labelEnd);

        if(peekTokenIs(ELSE)){
            expectPeek(ELSE);

            vmWriter.writeLabel(labelFalse);
            expectPeek(LBRACE);
            parseStatements();
            expectPeek(RBRACE);
            vmWriter.writeLabel(labelEnd);
        }

        printNonTerminal("/ifStatement");

        ifCounter += 1;
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
        } else {
            vmWriter.writePush(Segment.CONST, 0);
        }

        vmWriter.writeReturn();

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
            switch(peekToken.type){
                case PLUS:
                    vmWriter.writeArithmetic(Command.ADD);
                    break;
                case MINUS:
                    vmWriter.writeArithmetic(Command.SUB);
                    break;
                case ASTERISK:
                    vmWriter.writeCall("Math.multiply", 2);
                    break;
                case SLASH:
                    vmWriter.writeCall("Math.divide", 2);
                    break;
                case AND:
                    vmWriter.writeArithmetic(Command.AND);
                    break;
                case OR:
                    vmWriter.writeArithmetic(Command.OR);
                    break;
                case LT:
                    vmWriter.writeArithmetic(Command.LT);
                    break;
                case GT:
                    vmWriter.writeArithmetic(Command.GT);
                    break;
                case EQ:
                    vmWriter.writeArithmetic(Command.EQ);
                    break;
                case NOT:
                    vmWriter.writeArithmetic(Command.NOT);
                    break;
                default:
                    break;
            }
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
                vmWriter.writePush(Segment.CONST, Integer.parseInt(currentToken.value()));
                break;
            case STRING:
                expectPeek(STRING);

                // printTerminal(curToken, toPrint);
                String strValue = currentToken.value();

                vmWriter.writePush(Segment.CONST, strValue.length());
                vmWriter.writeCall("String.new", 1);
                for(int i = 0; i < strValue.length(); i++){
                    vmWriter.writePush(Segment.CONST, strValue.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE, NULL, TRUE);
                // printTerminal(curToken, toPrint);
                vmWriter.writePush(Segment.CONST, 0);
                if(currentTokenIs(TRUE)){
                    vmWriter.writeArithmetic(Command.NOT);
                }
                break;
            case THIS:
                expectPeek(THIS);
                // printTerminal(curToken, toPrint);
                vmWriter.writePush(Segment.POINTER, 0);
                break;
            case IDENTIFIER:
                expectPeek(IDENTIFIER);
                Symbol symbol = symbolTable.resolve(currentToken.value());
                
                if(peekTokenIs (LPAREN) || peekTokenIs(DOT)){
                    parseSubroutineCall();
                } else {
                    if (peekTokenIs (LBRACKET) ) {
                        expectPeek(LBRACKET);
                        parseExpression();

                        vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
                        vmWriter.writeArithmetic(Command.ADD);

                        expectPeek(RBRACKET);

                        vmWriter.writePop(Segment.POINTER, 1);
                        vmWriter.writePush(Segment.THAT, 0);
                    } else {
                        vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
                    }
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

    public String VMOutput() {
        return vmWriter.vmOutput();
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

    private Segment kind2Segment (Kind kind) {
        if (kind == Kind.STATIC) return Segment.STATIC;
        if (kind == Kind.FIELD) return Segment.THIS;
        if (kind == Kind.VAR) return Segment.LOCAL;
        if (kind == Kind.ARG) return Segment.ARG;
        return null;
    }
}