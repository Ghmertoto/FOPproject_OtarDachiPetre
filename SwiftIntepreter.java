import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class SwiftInterpreter {
    private List<Token> tokens; //list of obejects
    private int pos;//int representing current position in the token list
    private final Map<String, Object> globalVariables;//Stores global variables
    private Stack<Map<String, Object>> scopeStack;//Stack of scopes

    //constructor initializing the interpreter with a list of tokens
    public SwiftInterpreter(List<Token> tokens) {
        this.tokens = tokens; //tokens to interpret
        this.pos = 0;//set position from the beginning 0
        this.globalVariables = new HashMap<>();//empty map for global variables
        this.scopeStack = new Stack<>();//empty stack to store scopes
        this.scopeStack.push(globalVariables);//pushes global variables into the global scope
    }

    ///returns current scope
    private Map<String, Object> currentScope() {
        return scopeStack.peek();//retrieves top object, so scope from the stack
    }

    //creates a new nested scope by copying the current scope and pushing it onto the stack
    private void pushScope() {
        Map<String, Object> newScope = new HashMap<>(currentScope());  // Create a copy of current scope
        scopeStack.push(newScope);
    }
    //removes current, so top scope from the stack
    private void popScope() {
        if (scopeStack.size() > 1) {//makes sure last scopes stays
            scopeStack.pop();
        }
    }

    //processes the token list and executes
    public void execute() {
        try {
            while (pos < tokens.size() && !tokens.get(pos).type.equals("EOF")) {//loop continues until there are tokens left, and the current token is not End Of File
                Token token = tokens.get(pos);//get token from current position
                switch (token.type) {//identifies correct type of token and executes respective function
                    case "KEYWORD":
                        handleKeyword(token);
                        break;
                    case "IDENTIFIER":
                        handleAssignment();
                        break;
                    default://unknown tokens are ignored
                        pos++;
                        break;
                }
            }
        } catch (SwiftInterpreterException e) {//catches any error during execution that throw SwiftInterpreterException
            System.err.println(e.getMessage());//prints error
        } finally {
            while (scopeStack.size() > 1) {//removes all scopes until the only global scope is left
                popScope();
            }
        }
    }

    private void handleKeyword(Token token) {//processes keyword tokens
        switch (token.value) {
            case "var"://launches aproppraite hendler
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
            default://if the keyword is unrecognized the error is thrown
                throw new SwiftInterpreterException("Unhandled keyword: " + token.value, token.line, token.column);
                //meaningful error with line and column, where the error occured
        }
    }


    //checks if the variable exists in any active scope
    private boolean variableExists(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) { //searches from current scope to global scope
            if (scopeStack.get(i).containsKey(name)) {//if the current scope contains the variable returns true
                return true;
            }
        }
        return false;//if variable is not in this scope return false
    }

    //handles variable assignment
    private void handleAssignment() {
        Token nameToken = tokens.get(pos);//gets current token
        String varName = nameToken.value;//current tokens value is stored in varName

        //checks if the variable is defined in any active scope
        if (!variableExists(varName)) {//if the variable doesnt exist it throws exception, with line, column and meaningful message
            throw new SwiftInterpreterException("Undefined variable: " + varName, nameToken.line, nameToken.column);
        }

        pos++; //progresses to the next token
        expect("OPERATOR", "=");//makes sure next token is '=' or else throws an error
        Object value = evaluateExpression();//evaluates RHS
        updateVariable(varName, value);//assigns the computed value to the variable
        System.out.println("Updated variable: " + varName + " = " + value);//for debuging
    }

    //updates the value of a variable where it is defined
    private void updateVariable(String name, Object value) {
        // Start from innermost scope and work outward
        for (int i = scopeStack.size() - 1; i >= 0; i--) {//the loop starts from the innermost scope, checking if the variable exists in the scope
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {//if the value is found:
                scope.put(name, value);//the value that has been found is updated
                for (int j = i - 1; j >= 0; j--) {//after updating in the innermost scope it upgrades the value until global sscope
                    if (scopeStack.get(j).containsKey(name)) {
                        scopeStack.get(j).put(name, value);
                    }
                }
                return; //exit early after updating all the values
            }
        }
    }



    //method retrieves the value of a variable by searching through scopes
    private Object lookupVariable(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {//starts iterating through scope stack from top to bottom
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);//if the variable exists returns its value
            }
        }
        return null;//if not found return null
    }

    //handles var declarations
    private void handleVarDeclaration() {
        expect("KEYWORD", "var");//detects 'var' keyword
        Token nameToken = expect("IDENTIFIER");//next token indentifier expected
        expect("OPERATOR", "=");//next token '=' expected
        Object value = evaluateExpression();//evaluate RHS

        String varName = nameToken.value;
        if (currentScope().containsKey(varName)) {//if the variable exists in the current scope an exception is thrown
            throw new SwiftInterpreterException("Variable already declared in current scope: " + varName,
                    nameToken.line, nameToken.column);
        }
        currentScope().put(varName, value);//new variable gets added to the current scope
        System.out.println("Declared variable: " + varName + " = " + value);//prints the newly declared variable with its value
    }

    //replaces the current current list of tokens with a new list
    public void updateTokens(List<Token> newTokens) {
        this.tokens = newTokens;
        this.pos = 0;//sets the position to the beginning
    }

    //handles immutable declarations using let
    private void handleLetDeclaration() {
        expect("KEYWORD", "let");//checks for 'let' as a first token
        Token nameToken = expect("IDENTIFIER");//checks for and keeps the new identifier
        expect("OPERATOR", "=");//expects '=' as a next operator
        Object value = evaluateExpression();//does RHS evaluation and stores that value

        String letName = nameToken.value;
        if (currentScope().containsKey(letName)) {//checks if the letName is in the current scope, if so throws an error
            throw new SwiftInterpreterException("Variable already declared in current scope: " + letName,
                    nameToken.line, nameToken.column);
        }
        currentScope().put(letName, value);//the variable is added to the current scope
        System.out.println("Declared variable: " + letName + " = " + value);//outputs newly declared variable with its value
    }


    //handles while loops
    private void handleWhileLoop() {
        Token whileToken = expect("KEYWORD", "while");//ensures first token is while

        int conditionStartPos = pos;//saves the position for the future re-evaluation

        boolean condition = evaluateCondition();//evaluates the loop condition

        expect("PUNCTUATION", "{");//expects { at the start of the loop body

        int loopStartPos = pos;//saves the start of the loop body for iterative re-evaluation of the code
        int iterationCount = 0;
        final int MAX_ITERATIONS = 10000;//setting upper bound for maximum iterations to avoid endless loop

        while (condition) {
            if (++iterationCount > MAX_ITERATIONS) {//increment the iteration counter
                throw new SwiftInterpreterException("Maximum loop iteration count exceeded",//if it exceeds  throw an error
                        whileToken.line, whileToken.column);
            }

            pushScope();//create a new local scope
            pos = loopStartPos;//reset to the start of the body
            executeStatements();//run all the statements inside the loop
            popScope();//remove the local scope after one iteration

            pos = conditionStartPos;//reset position at the beginning of the loop, so at the condition
            condition = evaluateCondition();//re-evaluate condition
        }

        // Skip the loop body by searching for }
        while (pos < tokens.size()) {
            Token token = tokens.get(pos);
            if (token.type.equals("PUNCTUATION") && token.value.equals("}")) {
                pos++;
                break;
            }
            pos++;
        }
    }





    //handles the logic for if and else blocks
    private void handleIfStatement() {
        expect("KEYWORD", "if");//expects first token to be 'if'

        boolean condition = evaluateCondition();//evaluate the condition and return true/false

        expect("PUNCTUATION", "{");//start if block
        pushScope();//create a new scope for variables declared in the block

        //either execute or skip the "if" block
        try {
            if (condition) {//if the condition is ture
                executeStatements();//runs the code inside the block
                expect("PUNCTUATION", "}");//expects } at the end of the block

                if (pos < tokens.size() &&
                        tokens.get(pos).type.equals("KEYWORD") &&
                        tokens.get(pos).value.equals("else")) {
                    skipElseBlock();//skip else block
                }
            } else {
                skipBlock();//skip over 'if' block

                if (pos < tokens.size() &&
                        tokens.get(pos).type.equals("KEYWORD") &&
                        tokens.get(pos).value.equals("else")) {
                    expect("KEYWORD", "else");//if else is followed execute it
                    expect("PUNCTUATION", "{");//expect { as start for else block
                    pushScope();//create a new scope
                    try {
                        executeStatements();//execute the block
                        expect("PUNCTUATION", "}");//expect } as a finish
                    } finally {
                        popScope();//quit scope
                    }
                }
            }
        } finally {
            popScope();//quit scope at the very end of if-else condition
        }
    }

    //handle print statement
    private void handlePrint() {
        expect("KEYWORD", "print");//expect "print" keyword
        expect("PUNCTUATION", "(");//expect (
        Object value = evaluateExpression();//evaluate expression like print(5+5)
        System.out.println(value);//output the value
        expect("PUNCTUATION", ")");//expect ) as an end
        System.out.println("Print executed: " + value);
    }


    //executes statements until closing brace is encountered
    private void executeStatements() {
        while (pos < tokens.size() &&//loop continues as long as pos is whithin bounds of token list
                !(tokens.get(pos).type.equals("PUNCTUATION") &&
                        tokens.get(pos).value.equals("}"))) {//continue loop until } is encountered and check if it is
            Token token = tokens.get(pos);//retrieves the current token at position pos
            if (token.type.equals("KEYWORD")) {
                handleKeyword(token);//handle keywords
            } else if (token.type.equals("IDENTIFIER")) {
                handleAssignment();//handle variables
            } else {
                pos++;//skip over tokens that are neither keywords nor identifiers
            }
        }
    }

    //evaluates the conditions within if or while statement
    private boolean evaluateCondition() {
        Object leftValue = evaluateExpression();//evaluate left hand side
        if (pos >= tokens.size()) {//f the position exceeds the sze of the token list
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);//throw exception if pos exceeds the size of the token list
        }

        Token operatorToken = tokens.get(pos);
        if (!operatorToken.type.equals("OPERATOR")) {//make sure next token is an operator or else throw exception
            throw new SwiftInterpreterException("Expected operator but found: " + operatorToken.type,
                    operatorToken.line, operatorToken.column);
        }
        pos++;

        Object rightValue = evaluateExpression();

        return compareValues(leftValue, operatorToken.value, rightValue, operatorToken);
    }


    //actual comparison between the left hand side and right hand sides
    private boolean compareValues(Object left, String operator, Object right, Token operatorToken) {
        // If both are integers or can be represented as integers without loss
        boolean isIntegerComparison = (left instanceof Integer ||//checks it both values are integers or floats that can be represented as integers
                (left instanceof Double && ((Double)left) % 1 == 0)) &&
                (right instanceof Integer ||
                        (right instanceof Double && ((Double)right) % 1 == 0));

        if (isIntegerComparison) {
            long leftVal = ((Number) left).longValue();//connvert left to long
            long rightVal = ((Number) right).longValue();//connvert right to long

            return switch (operator) {//check operator and evaluate accordingly
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
            double leftVal = ((Number) left).doubleValue();//convert left to double
            double rightVal = ((Number) right).doubleValue();//convert right to double

            return switch (operator) {//check the operator and evaluate accordingly
                case "<" -> leftVal < rightVal;
                case ">" -> leftVal > rightVal;
                case "<=" -> leftVal <= rightVal;
                case ">=" -> leftVal >= rightVal;
                case "==" -> Math.abs(leftVal - rightVal) < 1e-10;
                case "!=" -> Math.abs(leftVal - rightVal) >= 1e-10;
                default -> throw new SwiftInterpreterException("Unknown comparison operator: " + operator,
                        operatorToken.line, operatorToken.column);//default if an unknow operator appears and throw exception
            };
        }
    }

    //handles the evaluation of expressions
    private Object evaluateExpression() {
        Object result = evaluateTerm();//evaluates the first term and stores it in the "result"

        while (pos < tokens.size() && tokens.get(pos).type.equals("OPERATOR")) {//loop iterates until all the tokens are processed
            Token operator = tokens.get(pos);
            if (isComparisonOperator(operator.value)) {//checks if it is comparison operator
                break;//if it is comarison operator breaks, since they are handled separetly
            }
            pos++;//move to next token
            Object rightValue = evaluateTerm();//evaluate the right operand and store it

            try {//apply the operators to the left and right operands
                result = applyOperator(result, operator.value, rightValue, operator);
            } catch (ArithmeticException e) {//if there is an arithmetic error, throw exception
                throw new SwiftInterpreterException("Arithmetic error: " + e.getMessage(),
                        operator.line, operator.column);
            }
        }

        return result;//return final result after processing all terms and operators
    }


    //helper function to check if it is comparison operaator
    private boolean isComparisonOperator(String operator) {
        return operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=") ||
                operator.equals("==") || operator.equals("!=");
    }

    //evaluates individual tokens
    private Object evaluateTerm() {
        if (pos >= tokens.size()) {//checks if pos is beyond the token list size
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        }

        Token token = tokens.get(pos++);//current token is retrieved
        return switch (token.type) {//if the token is an INTEGER turn it into Ineteger
            case "INTEGER" -> {
                try {
                    yield Integer.parseInt(token.value);
                } catch (NumberFormatException e) {
                    yield Long.parseLong(token.value);
                }
            }
            case "FLOAT" -> Double.parseDouble(token.value);//if the token is FLOAT parse token value as Double
            case "STRING" -> token.value;//f the token is STRING parse taken value into String
            case "IDENTIFIER" -> {
                Object value = lookupVariable(token.value);
                if (value == null) {//in case of an identifier find the variable else throw an exception
                    throw new SwiftInterpreterException("Undefined variable: " + token.value,
                            token.line, token.column);
                }
                yield value;
            }
            default -> throw new SwiftInterpreterException("Unexpected token type: " + token.type,
                    token.line, token.column);//if the token type is unrecognized throw an exception
        };
    }

    //applies arithmetic operators to two operands
    private Object applyOperator(Object left, String operator, Object right, Token operatorToken) {
        if (!(left instanceof Number) || !(right instanceof Number)) {//checks if both operands are instances of number
            throw new SwiftInterpreterException("Invalid operands for operator " + operator,
                    operatorToken.line, operatorToken.column);
        }
        //cast both of the values into ints
        int leftVal = ((Number) left).intValue();
        int rightVal = ((Number) right).intValue();

        //find correct operator and perform appropriate operation
        return switch (operator) {
            case "+" -> leftVal + rightVal;
            case "-" -> leftVal - rightVal;
            case "*" -> leftVal * rightVal;
            case "/" -> {
                if (rightVal == 0) {//check for dividing by zero
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


    //checks that the current token matches the expected type
    private Token expect(String type) {
        if (pos >= tokens.size()) {//if the token is beyond the list of tokens
            throw new SwiftInterpreterException("Unexpected end of input",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        }
        Token token = tokens.get(pos);
        if (!token.type.equals(type)) {//if not matched throw and exception
            throw new SwiftInterpreterException("Expected " + type + " but found " + token.type,
                    token.line, token.column);
        }
        pos++;
        return token;
    }

    //overloaded expect() method
    //checks the type of the token and also its value
    private Token expect(String type, String value) {
        Token token = expect(type);
        if (!token.value.equals(value)) {//if the value doesnt match throw an error
            throw new SwiftInterpreterException("Expected " + value + " but found " + token.value,
                    token.line, token.column);
        }
        return token;//if no errors were encountered return token
    }


    //helper function for if-else statemetns, skips the else block
    private void skipElseBlock() {
        int skipCount = 1;//starts with one since the else block was entered
        while (pos < tokens.size() && skipCount > 0) {//iterates over the tokens
            Token token = tokens.get(pos++);
            if (token.type.equals("PUNCTUATION")) {
                if (token.value.equals("{")) skipCount++;//incremented, since it entered another block
                else if (token.value.equals("}")) skipCount--;//decremented since it left the block
            }
        }
    }

    //skips an antire either an if or wile block
    private void skipBlock() {
        int braceCount = 1;//same counter method used as in skipElseBlock
        while (pos < tokens.size() && braceCount > 0) {
            Token token = tokens.get(pos++);
            if (token.type.equals("PUNCTUATION")) {
                if (token.value.equals("{")) {//if there is { it means the code entered another block
                    braceCount++;
                    System.out.println("Opening brace found, braceCount: " + braceCount);
                } else if (token.value.equals("}")) {//if } it means the code left another block
                    braceCount--;
                    System.out.println("Closing brace found, braceCount: " + braceCount);
                }
            }
        }

        if (braceCount > 0) {//if there is one more } remaining it means there is extra, so throws an error
            throw new SwiftInterpreterException("Unclosed block",
                    tokens.get(pos - 1).line, tokens.get(pos - 1).column);
        } else {
            System.out.println("Block closed properly, braceCount: " + braceCount);
        }
    }






    static class SwiftInterpreterException extends RuntimeException {
        //constructor formats an error message with line and column, where the error happened
        public SwiftInterpreterException(String message, int line, int column) {
            super(String.format("Error at line %d, column %d: %s", line, column, message));
        }

        public SwiftInterpreterException(String message) {
            super(message);
        }
    }
}