class TokenizerException extends RuntimeException {
    final int line;
    final int column;

    TokenizerException(String message, int line, int column) {
        super(String.format("Error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }
}
