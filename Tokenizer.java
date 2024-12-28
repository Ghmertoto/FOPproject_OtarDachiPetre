import java.util.*;

class Tokenizer {

    private final String input;  // The input string to be tokenized, stored as a final field since it won't change.
    private int pos;   //  Tracks the current position in the input string during tokenization.
    private int line;   // Tracks the current line number in the input.
    private int column;  // Tracks the current column number in the input.

    // Set of predefined keywords in the language, stored in a HashSet for fast lookup.
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "var", "if", "else", "while", "for", "print", "function",
            "return", "break", "continue", "true", "false"
    ));

    // Map of multi-character operators to their corresponding symbolic names.
    private static final Map<String, String> OPERATORS = new HashMap<>() {{
        put("==", "EQUALS");
        put("!=", "NOT_EQUALS");
        put(">", "GREATER");
        put("<", "LESS");
        put("<=", "LESS_EQUAL");
        put(">=", "GREATER_EQUAL");
        put("&&", "AND");
        put("||", "OR");
        put("++", "INCREMENT");
        put("--", "DECREMENT");
        put("+=", "PLUS_EQUALS");
        put("-=", "MINUS_EQUALS");
        put("*=", "MULTIPLY_EQUALS");
        put("/=", "DIVIDE_EQUALS");
    }};

    // Constructor: Initializes the tokenizer with the input string and sets position, line, and column.
    Tokenizer(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;    // Starts from the first line.
        this.column = 1;  // Starts from the first column.
    }

    // Main method for tokenizing the input into a list of tokens.
    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>(); // List to store the generated tokens.

        // Iterating through the input until the end is reached.
        while (pos < input.length()) {
            char current = peek(0); // Peek at the current character without advancing.

            if (Character.isWhitespace(current)) {
                consumeWhitespace(); // Handle whitespace and update line/column information.
            } else if (current == '/' && peek(1) == '/') {
                consumeSingleLineComment(); // Skip single-line comments starting with `//`.
            } else if (current == '/' && peek(1) == '*') {
                consumeMultiLineComment(); // Skip multi-line comments enclosed in `/* */`.
            } else if (current == '"' || current == '\'') {
                tokens.add(tokenizeString()); // Extract and tokenize string literals.
            } else if (Character.isDigit(current)) {
                tokens.add(tokenizeNumber()); // Extract and tokenize numeric literals.
            } else if (isIdentifierStart(current)) {
                tokens.add(tokenizeIdentifier()); // Extract and tokenize identifiers or keywords.
            } else if (isOperatorStart(current)) {
                tokens.add(tokenizeOperator()); // Extract and tokenize operators.
            } else if (isPunctuation(current)) {
                // Tokenize punctuation characters (e.g., '(', ')', ';', etc.).
                tokens.add(new Token("PUNCTUATION", String.valueOf(consumeChar()), line, column - 1));
            } else {
                // Throw TokenizeException which extends RuntimeException.
                throw new TokenizerException("Unexpected character: " + current, line, column);
            }
        }
        // Add an end-of-file (EOF) token to indicate the end of the input stream.
        tokens.add(new Token("EOF", "", line, column));
        return tokens;
    }

    // Checks if a character can start an identifier (letters, underscore, or dollar sign).
    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    // Checks if a character can be part of an identifier (letters, digits, underscore, or dollar sign).
    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    // Checks if a character can start an operator (common operator symbols).
    private boolean isOperatorStart(char c) {
        return "+-*/%=<>!&|^~".indexOf(c) != -1;
    }

    // Checks if a character is punctuation (common punctuation symbols).
    private boolean isPunctuation(char c) {
        return "(){}[];,.".indexOf(c) != -1;
    }

    // Tokenizes string literals, handling escape sequences and quoted strings.
    private Token tokenizeString() {
        int startColumn = column; // Record the starting column for position tracking.
        char quote = consumeChar(); // Consume the opening quote (either single or double quote).
        StringBuilder value = new StringBuilder(); // Builder for the string content.
        boolean escaped = false; // Tracks whether the current character is escaped.

        while (pos < input.length()) {
            char current = consumeChar();

            if (escaped) {
                value.append(parseEscapeSequence(current)); // Parse and append escape sequence.
                escaped = false;
            } else if (current == '\\') {
                escaped = true; // Mark the next character as escaped.
            } else if (current == quote) {
                // Return a STRING token when the closing quote is found.
                return new Token("STRING", value.toString(), line, startColumn);
            } else {
                value.append(current); // Append the current character to the string value.
            }
        }
        // Throw exception that the string literal was unterminated.
        throw new TokenizerException("Unterminated string literal", line, startColumn);
    }

    // Parses common escape sequences like `\n`, `\t`, and `\\`.
    private char parseEscapeSequence(char c) {
        return switch (c) {
            case 'n' -> '\n';  // Newline
            case 't' -> '\t';  // Tab
            case 'r' -> '\r';  // Carriage return
            case 'b' -> '\b';  // Backspace
            case 'f' -> '\f';  // Form feed
            case '\'', '\"', '\\' -> c; // Escaped single quote, double quote, or backslash
            default -> throw new IllegalStateException("Unexpected escape sequence: " + c);
        };
    }

    // Tokenizes numeric literals, including integers, floating-point numbers, and scientific notation.
    private Token tokenizeNumber() {
        int startColumn = column;
        StringBuilder value = new StringBuilder();
        boolean hasDecimalPoint = false;

        while (pos < input.length()) {
            char current = peek(0);

            if (current == '.' && !hasDecimalPoint) {
                hasDecimalPoint = true; // Mark the presence of a decimal point.
                value.append(consumeChar());
            } else if (Character.isDigit(current)) {
                value.append(consumeChar());
            } else {
                break;
            }
        }

        // Handle scientific notation if present.
        if (pos < input.length() && (peek(0) == 'e' || peek(0) == 'E')) {
            value.append(consumeChar()); // Consume 'e' or 'E'.
            if (pos < input.length() && (peek(0) == '+' || peek(0) == '-')) {
                value.append(consumeChar()); // Consume optional sign.
            }
            while (pos < input.length() && Character.isDigit(peek(0))) {
                value.append(consumeChar()); // Consume exponent digits.
            }
        }

        return new Token(hasDecimalPoint ? "FLOAT" : "INTEGER", value.toString(), line, startColumn);
    }

    // Tokenizes identifiers or keywords.
    private Token tokenizeIdentifier() {
        int startColumn = column;
        StringBuilder value = new StringBuilder();

        while (pos < input.length() && isIdentifierPart(peek(0))) {
            value.append(consumeChar());
        }

        String identifier = value.toString();
        if (KEYWORDS.contains(identifier)) {
            return new Token("KEYWORD", identifier, line, startColumn);
        }

        return new Token("IDENTIFIER", identifier, line, startColumn);
    }

    // Tokenizes operators, prioritizing multi-character operators.
    private Token tokenizeOperator() {
        int startColumn = column;
        StringBuilder value = new StringBuilder();

        // Try to match two-character operators first.
        if (pos + 1 < input.length()) {
            String twoChars = input.substring(pos, pos + 2);
            if (OPERATORS.containsKey(twoChars)) {
                pos += 2;
                column += 2;
                return new Token("OPERATOR", twoChars, line, startColumn);
            }
        }

        // Single-character operator.
        char operator = consumeChar();
        return new Token("OPERATOR", String.valueOf(operator), line, startColumn);
    }

    // Consumes whitespace and updates line/column tracking.
    private void consumeWhitespace() {
        while (pos < input.length() && Character.isWhitespace(peek(0))) {
            char current = consumeChar();
            if (current == '\n') {
                line++; // Increment line count on newlines.
                column = 1; // Reset column to 1.
            }
        }
        throw new TokenizerException("Unterminated multi-line comment", line, column);
    }

    // Consumes single-line comments.
    private void consumeSingleLineComment() {
        consumeChar(); // Consume first '/'
        consumeChar(); // Consume second '/'

        while (pos < input.length() && peek(0) != '\n') {
            consumeChar(); // Consume characters until the end of the line.
        }
    }

    // Consumes multi-line comments.
    private void consumeMultiLineComment() {
        consumeChar(); // Consume '/'
        consumeChar(); // Consume '*'

        while (pos < input.length() - 1) {
            if (peek(0) == '*' && peek(1) == '/') {
                consumeChar(); // Consume '*'
                consumeChar(); // Consume '/'
                return;
            }
            char current = consumeChar();
            if (current == '\n') {
                line++; // Increment line count on newlines.
                column = 1; // Reset column to 1.
            }
        }
    }

    // Peeks at a character ahead of the current position without consuming it.
    private char peek(int ahead) {
        if (pos + ahead >= input.length()) {
            return '\0'; // Return null character if out of bounds.
        }
        return input.charAt(pos + ahead);
    }

    // Consumes the current character and advances the position and column.
    private char consumeChar() {
        char c = input.charAt(pos++);
        column++;
        return c;
    }
}
