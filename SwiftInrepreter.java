import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class SwiftInterpreter {
    private final List<Token> tokens;
    private int pos;
    private final Map<String, Object> globalVariables;
    private Stack<Map<String, Object>> scopeStack;

    public SwiftInterpreter(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.globalVariables = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(globalVariables);
    }

    private Map<String, Object> currentScope() {
        return scopeStack.peek();
    }

    private void pushScope() {
        Map<String, Object> newScope = new HashMap<>(currentScope());  // Create a copy of current scope
        scopeStack.push(newScope);
    }
    private void popScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
        }
    }

    public void execute() {
        try {
            while (pos < tokens.size() && !tokens.get(pos).type.equals("EOF")) {
                Token token = tokens.get(pos);
                switch (token.type) {
                    case "KEYWORD":
                        handleKeyword(token);
                        break;
                    case "IDENTIFIER":
                        handleAssignment();
                        break;
                    default:
                        pos++;
                        break;
                }
            }
        } catch (SwiftInterpreterException e) {
            System.err.println(e.getMessage());
        } finally {
            while (scopeStack.size() > 1) {
                popScope();
            }
        }
    }

    private void handleKeyword(Token token) {
        switch (token.value) {
            case "var":
                handleVarDeclaration();
                break;
            case "while":
                handleWhileLoop();
                break;
            case "let":
                handleLetDeclaration();
                break;
            case "if":
                handleIfStatement();
                break;
            case "print":
                handlePrint();
                break;
            default:
                throw new SwiftInterpreterException("Unhandled keyword: " + token.value, token.line, token.column);
        }
    }


    private boolean variableExists(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    private void handleAssignment() {
        Token nameToken = tokens.get(pos);
        String varName = nameToken.value;

        if (!variableExists(varName)) {
            throw new SwiftInterpreterException("Undefined variable: " + varName, nameToken.line, nameToken.column);
        }

        pos++;
        expect("OPERATOR", "=");
        Object value = evaluateExpression();
        updateVariable(varName, value);
        // Debug print statement
        System.out.println("Updated variable: " + varName + " = " + value);
    }

    private void updateVariable(String name, Object value) {
        // Start from innermost scope and work outward
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                scope.put(name, value);
                // Update all outer scopes that contain this variable
                for (int j = i - 1; j >= 0; j--) {
                    if (scopeStack.get(j).containsKey(name)) {
                        scopeStack.get(j).put(name, value);
                    }
                }
                return;
            }
        }
    }



    private Object lookupVariable(String name) {
        // Start from innermost scope and work outward
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    private void handleVarDeclaration() {
        expect("KEYWORD", "var");
        Token nameToken = expect("IDENTIFIER");
        expect("OPERATOR", "=");
        Object value = evaluateExpression();

        String varName = nameToken.value;
        if (currentScope().containsKey(varName)) {
            throw new SwiftInterpreterException("Variable already declared in current scope: " + varName,
                    nameToken.line, nameToken.column);
        }
        currentScope().put(varName, value);
        // Debug print statement
        System.out.println("Declared variable: " + varName + " = " + value);
    }

    private void handleLetDeclaration() {
        expect("KEYWORD", "let");
        Token nameToken = expect("IDENTIFIER");
        expect("OPERATOR", "=");
        Object value = evaluateExpression();

        String letName = nameToken.value;
        if (currentScope().containsKey(letName)) {
            throw new SwiftInterpreterException("Variable already declared in current scope: " + letName,
                    nameToken.line, nameToken.column);
        }
        currentScope().put(letName, value);
        // Debug print statement
        System.out.println("Declared variable: " + letName + " = " + value);
    }


    private void handleWhileLoop() {
        Token whileToken = expect("KEYWORD", "while");

        int conditionStartPos = pos;

        boolean condition = evaluateCondition();

        expect("PUNCTUATION", "{");

        int loopStartPos = pos;
        int iterationCount = 0;
        final int MAX_ITERATIONS = 10000;

        while (condition) {
            if (++iterationCount > MAX_ITERATIONS) {
                throw new SwiftInterpreterException("Maximum loop iteration count exceeded",
                        whileToken.line, whileToken.column);
            }

            pushScope();
            pos = loopStartPos;
            executeStatements();
            popScope();

            pos = conditionStartPos;
            condition = evaluateCondition();
        }

        // Skip past the loop body after it's done
        while (pos < tokens.size()) {
            Token token = tokens.get(pos);
            if (token.type.equals("PUNCTUATION") && token.value.equals("}")) {
                pos++;
                break;
            }
            pos++;
        }
    }





    private void handleIfStatement() {
        expect("KEYWORD", "if");

        boolean condition = evaluateCondition();

        expect("PUNCTUATION", "{");
        pushScope();

        try {
            if (condition) {
                executeStatements();
                expect("PUNCTUATION", "}");

                if (pos < tokens.size() &&
                        tokens.get(pos).type.equals("KEYWORD") &&
                        tokens.get(pos).value.equals("else")) {
                    skipElseBlock();
                }
            } else {
                skipBlock();

                if (pos < tokens.size() &&
                        tokens.get(pos).type.equals("KEYWORD") &&
                        tokens.get(pos).value.equals("else")) {
                    expect("KEYWORD", "else");
                    expect("PUNCTUATION", "{");
                    pushScope();
                    try {
                        executeStatements();
                        expect("PUNCTUATION", "}");
                    } finally {
                        popScope();
                    }
                }
            }
        } finally {
            popScope();
        }
    }

    private void handlePrint() {
        expect("KEYWORD", "print");
        expect("PUNCTUATION", "(");
        Object value = evaluateExpression();
        System.out.println(value);
        expect("PUNCTUATION", ")");
        // Debug print statement
        System.out.println("Print executed: " + value);
    }


    private void executeStatements() {
        while (pos < tokens.size() &&
                !(tokens.get(pos).type.equals("PUNCTUATION") &&
                        tokens.get(pos).value.equals("}"))) {
            Token token = tokens.get(pos);
            if (token.type.equals("KEYWORD")) {
                handleKeyword(token);
            } else if (token.type.equals("IDENTIFIER")) {
                handleAssignment();
            } else {
                pos++;
            }
        }
    }

    private boolean evaluateCondition() {
        Object leftValue = evaluateExpression();
        if (pos >= tokens.size()) {
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        }

        Token operatorToken = tokens.get(pos);
        if (!operatorToken.type.equals("OPERATOR")) {
            throw new SwiftInterpreterException("Expected operator but found: " + operatorToken.type,
                    operatorToken.line, operatorToken.column);
        }
        pos++;

        Object rightValue = evaluateExpression();

        return compareValues(leftValue, operatorToken.value, rightValue, operatorToken);
    }


    private boolean compareValues(Object left, String operator, Object right, Token operatorToken) {
        // If both are integers or can be represented as integers without loss
        boolean isIntegerComparison = (left instanceof Integer ||
                (left instanceof Double && ((Double)left) % 1 == 0)) &&
                (right instanceof Integer ||
                        (right instanceof Double && ((Double)right) % 1 == 0));

        if (isIntegerComparison) {
            long leftVal = ((Number) left).longValue();
            long rightVal = ((Number) right).longValue();

            return switch (operator) {
                case "<" -> leftVal < rightVal;
                case ">" -> leftVal > rightVal;
                case "<=" -> leftVal <= rightVal;
                case ">=" -> leftVal >= rightVal;
                case "==" -> leftVal == rightVal;
                case "!=" -> leftVal != rightVal;
                default -> throw new SwiftInterpreterException("Unknown comparison operator: " + operator,
                        operatorToken.line, operatorToken.column);
            };
        } else {
            // Existing floating-point comparison code
            double leftVal = ((Number) left).doubleValue();
            double rightVal = ((Number) right).doubleValue();

            return switch (operator) {
                case "<" -> leftVal < rightVal;
                case ">" -> leftVal > rightVal;
                case "<=" -> leftVal <= rightVal;
                case ">=" -> leftVal >= rightVal;
                case "==" -> Math.abs(leftVal - rightVal) < 1e-10;
                case "!=" -> Math.abs(leftVal - rightVal) >= 1e-10;
                default -> throw new SwiftInterpreterException("Unknown comparison operator: " + operator,
                        operatorToken.line, operatorToken.column);
            };
        }
    }

    private Object evaluateExpression() {
        Object result = evaluateTerm();

        while (pos < tokens.size() && tokens.get(pos).type.equals("OPERATOR")) {
            Token operator = tokens.get(pos);
            if (isComparisonOperator(operator.value)) {
                break;
            }
            pos++;
            Object rightValue = evaluateTerm();

            try {
                result = applyOperator(result, operator.value, rightValue, operator);
            } catch (ArithmeticException e) {
                throw new SwiftInterpreterException("Arithmetic error: " + e.getMessage(),
                        operator.line, operator.column);
            }
        }

        return result;
    }


    private boolean isComparisonOperator(String operator) {
        return operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=") ||
                operator.equals("==") || operator.equals("!=");
    }

    private Object evaluateTerm() {
        if (pos >= tokens.size()) {
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        }

        Token token = tokens.get(pos++);
        return switch (token.type) {
            case "INTEGER" -> {
                try {
                    yield Integer.parseInt(token.value);
                } catch (NumberFormatException e) {
                    yield Long.parseLong(token.value);
                }
            }
            case "FLOAT" -> Double.parseDouble(token.value);
            case "STRING" -> token.value;
            case "IDENTIFIER" -> {
                Object value = lookupVariable(token.value);
                if (value == null) {
                    throw new SwiftInterpreterException("Undefined variable: " + token.value,
                            token.line, token.column);
                }
                yield value;
            }
            default -> throw new SwiftInterpreterException("Unexpected token type: " + token.type,
                    token.line, token.column);
        };
    }

    private Object applyOperator(Object left, String operator, Object right, Token operatorToken) {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new SwiftInterpreterException("Invalid operands for operator " + operator,
                    operatorToken.line, operatorToken.column);
        }

        int leftVal = ((Number) left).intValue();
        int rightVal = ((Number) right).intValue();

        return switch (operator) {
            case "+" -> leftVal + rightVal;
            case "-" -> leftVal - rightVal;
            case "*" -> leftVal * rightVal;
            case "/" -> {
                if (rightVal == 0) {
                    throw new SwiftInterpreterException("Division by zero",
                            operatorToken.line, operatorToken.column);
                }
                yield leftVal / rightVal; // Integer division
            }
            case "%" -> {
                if (rightVal == 0) {
                    throw new SwiftInterpreterException("Modulo by zero",
                            operatorToken.line, operatorToken.column);
                }
                yield Math.floorMod(leftVal, rightVal); // changed to int because it was hard to compare in condition
            }
            default -> throw new SwiftInterpreterException("Unknown operator: " + operator,
                    operatorToken.line, operatorToken.column);
        };
    }


    private Token expect(String type) {
        if (pos >= tokens.size()) {
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        }
        Token token = tokens.get(pos);
        if (!token.type.equals(type)) {
            throw new SwiftInterpreterException("Expected " + type + " but found " + token.type,
                    token.line, token.column);
        }
        pos++;
        return token;
    }

    private Token expect(String type, String value) {
        Token token = expect(type);
        if (!token.value.equals(value)) {
            throw new SwiftInterpreterException("Expected " + value + " but found " + token.value,
                    token.line, token.column);
        }
        return token;
    }

    private void skipElseBlock() {
        int skipCount = 1;
        while (pos < tokens.size() && skipCount > 0) {
            Token token = tokens.get(pos++);
            if (token.type.equals("PUNCTUATION")) {
                if (token.value.equals("{")) skipCount++;
                else if (token.value.equals("}")) skipCount--;
            }
        }
    }

    private void skipBlock() {
        int braceCount = 1;
        while (pos < tokens.size() && braceCount > 0) {
            Token token = tokens.get(pos++);
            if (token.type.equals("PUNCTUATION")) {
                if (token.value.equals("{")) {
                    braceCount++;
                    System.out.println("Opening brace found, braceCount: " + braceCount);
                } else if (token.value.equals("}")) {
                    braceCount--;
                    System.out.println("Closing brace found, braceCount: " + braceCount);
                }
            }
        }

        if (braceCount > 0) {
            throw new SwiftInterpreterException("Unclosed block",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        } else {
            System.out.println("Block closed properly, braceCount: " + braceCount);
        }
    }




    static class SwiftInterpreterException extends RuntimeException {
        public SwiftInterpreterException(String message, int line, int column) {
            super(String.format("Error at line %d, column %d: %s", line, column, message));
        }

        public SwiftInterpreterException(String message) {
            super(message);
        }
    }
}