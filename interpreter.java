import java.util.*;

class InterpreterException extends RuntimeException {
    InterpreterException(String message) {
        super(message);
    }
}

class Interpreter {
    private final List<Token> tokens;
    private int pos;
    private final Map<String, Object> variables;

    Interpreter(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.variables = new HashMap<>();
    }

    void interpret() {
        while (pos < tokens.size()) {
            Token token = tokens.get(pos);

            // Check for EOF token to break the loop
            if (token.type.equals("EOF")) {
                break;
            }

            // Process tokens based on their type
            if (token.type.equals("KEYWORD")) {
                switch (token.value) {
                    case "var" -> handleVariableDeclaration();
                    case "print" -> handlePrint();
                    case "if" -> handleIf();
                    case "while" -> handleWhile();
                    default -> throw new InterpreterException("Unexpected keyword: " + token.value);
                }
            } else if (token.type.equals("IDENTIFIER")) {
                handleAssignmentOrExpression();
            } else {
                pos++;
            }
        }
    }

    private void handleVariableDeclaration() {
        pos++; // Skip 'var'
        Token varToken = tokens.get(pos++);
        if (!varToken.type.equals("IDENTIFIER")) {
            throw new InterpreterException("Expected variable name after 'var'");
        }

        String varName = varToken.value;
        Token equalsToken = tokens.get(pos++);
        if (!equalsToken.type.equals("OPERATOR") || !equalsToken.value.equals("=")) {
            throw new InterpreterException("Expected '=' after variable name");
        }

        Object value = evaluateExpression();
        variables.put(varName, value);
    }

    private void handlePrint() {
        pos++; // Skip 'print'
        skipParenOpen();
        Object value = evaluateExpression();
        skipParenClose();
        System.out.println(value);
    }

    private void handleIf() {
        pos++; // Skip 'if'
        skipParenOpen();
        Object condition = evaluateExpression();
        skipParenClose();

        if (condition instanceof Boolean && (Boolean) condition) {
            executeBlock();
        } else {
            skipBlock();
        }
    }

    private void handleWhile() {
        pos++; // Skip 'while'
        skipParenOpen();
        Object condition = evaluateExpression();
        skipParenClose();

        while (condition instanceof Boolean && (Boolean) condition) {
            executeBlock();
            skipParenOpen();
            condition = evaluateExpression();
            skipParenClose();
        }
    }

    private void handleAssignmentOrExpression() {
        String varName = tokens.get(pos++).value;
        Token nextToken = tokens.get(pos);

        if (nextToken.type.equals("OPERATOR") && nextToken.value.equals("=")) {
            pos++; // Skip '='
            Object value = evaluateExpression();
            variables.put(varName, value);
        } else {
            pos--; // Reset for expression evaluation
            evaluateExpression();
        }
    }

    private void executeBlock() {
        // Execute the block (statements enclosed in braces)
        skipBraceOpen();
        while (pos < tokens.size() && !isClosingBrace()) {
            Token token = tokens.get(pos);
            if (token.type.equals("KEYWORD")) {
                switch (token.value) {
                    case "var" -> handleVariableDeclaration();
                    case "print" -> handlePrint();
                    case "if" -> handleIf();
                    case "while" -> handleWhile();
                    default -> throw new InterpreterException("Unexpected keyword: " + token.value);
                }
            } else {
                pos++;
            }
        }
        skipBraceClose();
    }

    private void skipBlock() {
        // Skip the block (statements enclosed in braces)
        skipBraceOpen();
        while (pos < tokens.size() && !isClosingBrace()) {
            pos++;
        }
        skipBraceClose();
    }

    private void skipBraceOpen() {
        Token token = tokens.get(pos);
        if (token.type.equals("PUNCTUATION") && token.value.equals("{")) {
            pos++;
        }
    }

    private void skipBraceClose() {
        Token token = tokens.get(pos);
        if (token.type.equals("PUNCTUATION") && token.value.equals("}")) {
            pos++;
        }
    }

    private boolean isClosingBrace() {
        Token token = tokens.get(pos);
        return token.type.equals("PUNCTUATION") && token.value.equals("}");
    }

    private Object evaluateExpression() {
        Object left = evaluateTerm();

        while (pos < tokens.size()) {
            Token token = tokens.get(pos);
            if (!token.type.equals("OPERATOR") ||
                    (!token.value.equals("==") && !token.value.equals("!=") &&
                            !token.value.equals("+") && !token.value.equals("-") &&
                            !token.value.equals("<") && !token.value.equals(">") &&
                            !token.value.equals("<=") && !token.value.equals(">="))) {
                break;
            }

            pos++;
            Object right = evaluateTerm();
            left = evaluateOperator(token, left, right);
        }

        return left;
    }

    private Object evaluateTerm() {
        Token token = tokens.get(pos++);

        return switch (token.type) {
            case "INTEGER" -> Integer.parseInt(token.value);
            case "FLOAT" -> Double.parseDouble(token.value);
            case "STRING" -> token.value;
            case "IDENTIFIER" -> {
                if (!variables.containsKey(token.value)) {
                    throw new InterpreterException("Undefined variable: " + token.value);
                }
                yield variables.get(token.value);
            }
            case "KEYWORD" -> switch (token.value) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new InterpreterException("Unexpected keyword in expression: " + token.value);
            };
            default -> throw new InterpreterException("Unexpected token in expression: " + token);
        };
    }

    private Object evaluateOperator(Token operator, Object left, Object right) {
        return switch (operator.value) {
            case "+" -> {
                if (left instanceof String || right instanceof String) {
                    yield String.valueOf(left) + String.valueOf(right);
                }
                if (left instanceof Double || right instanceof Double) {
                    yield ((Number) left).doubleValue() + ((Number) right).doubleValue();
                }
                yield ((Number) left).intValue() + ((Number) right).intValue();
            }
            case "-" -> {
                if (left instanceof Double || right instanceof Double) {
                    yield ((Number) left).doubleValue() - ((Number) right).doubleValue();
                }
                yield ((Number) left).intValue() - ((Number) right).intValue();
            }
            case "<" -> ((Number) left).doubleValue() < ((Number) right).doubleValue();
            case ">" -> ((Number) left).doubleValue() > ((Number) right).doubleValue();
            case "<=" -> ((Number) left).doubleValue() <= ((Number) right).doubleValue();
            case ">=" -> ((Number) left).doubleValue() >= ((Number) right).doubleValue();
            case "==" -> left.equals(right);
            case "!=" -> !left.equals(right);
            default -> throw new InterpreterException("Unsupported operator: " + operator.value);
        };
    }

    private void skipParenOpen() {
        Token token = tokens.get(pos);
        if (token.type.equals("PUNCTUATION") && token.value.equals("(")) {
            pos++;
        }
    }

    private void skipParenClose() {
        Token token = tokens.get(pos);
        if (token.type.equals("PUNCTUATION") && token.value.equals(")")) {
            pos++;
     }
  }
}
