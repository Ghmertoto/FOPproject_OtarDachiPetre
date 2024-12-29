
// This class represents a lexical token with type, value and position in the source code.
class Token {
    final String type; // Attribute, which represents the type of the token.
    final String value; // Attribute, which represents the value of the token.
    final int line; // Attribute, which represents the line number in the source code where the token appears.
    final int column; // Attribute, which represents the column number in the source code, where the token starts.

    // Constructor.
    Token(String type, String value, int line, int column) {

        // Handling illegal argument exception.
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        if (line < 0) {
            throw new IllegalArgumentException("Line number must be non-negative.");
        }
        if (column < 0) {
            throw new IllegalArgumentException("Column number must be non-negative.");
        }

        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    // This method returns a string interpretation of the token object.
    // The format includes type, value and position of the token.
    @Override
    public String toString() {
        return String.format("Token{type='%s', value='%s', position=(%d:%d)}",
                type, value, line, column);
    }
}