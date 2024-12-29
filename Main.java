import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class Main {
    private static Map<String, Object> globalState = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Swift Interpreter!");
        System.out.println("Enter your code (type 'exit' on a new line to quit):");

        StringBuilder codeBuilder = new StringBuilder();
        String line;
        SwiftInterpreter interpreter = new SwiftInterpreter(List.of()); // Initialize with empty tokens

        while (true) {
            System.out.print("> ");
            line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("exit")) {
                break;
            }

            if (!line.isEmpty()) {
                codeBuilder.append(line).append("\n");

                // Try to execute the code if it appears complete
                if (isCompleteCode(codeBuilder.toString())) {
                    try {
                        executeCode(codeBuilder.toString(), interpreter);
                        codeBuilder = new StringBuilder(); // Clear the buffer after successful execution
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                        codeBuilder = new StringBuilder(); // Clear on error to avoid cascading errors
                    }
                }
            }
        }

        System.out.println("Goodbye!");
        scanner.close();
    }


    private static boolean isCompleteCode(String code) {
        // Count braces to check for complete blocks
        int braces = 0;
        String[] lines = code.split("\n");

        for (String line : lines) {
            line = line.trim();
            for (char c : line.toCharArray()) {
                if (c == '{') braces++;
                if (c == '}') braces--;
            }
        }

        // If braces don't match, code block is incomplete
        if (braces != 0) return false;

        // Check if the last line indicates an incomplete statement
        String lastLine = lines[lines.length - 1].trim();
        return !lastLine.endsWith("{") &&
                !lastLine.startsWith("if") &&
                !lastLine.startsWith("while") &&
                !lastLine.startsWith("else");
    }

    private static void executeCode(String code, SwiftInterpreter interpreter) {
        try {
            // Create tokenizer and get tokens
            Tokenizer tokenizer = new Tokenizer(code);
            List<Token> tokens = tokenizer.tokenize();

            // Update interpreter with new tokens while preserving state
            interpreter.updateTokens(tokens);
            interpreter.execute();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}