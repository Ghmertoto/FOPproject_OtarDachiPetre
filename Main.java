
// This program, we have made, allows users to enter Swift-like code, which is then tokenized, interpreted, and executed.
// It handles code blocks, maintains a global state, and checks for code completeness before execution.

 /* Features:
  - Read and execute Swift-like code interactively.
  - Detect incomplete code blocks and wait for completion.
  - Handle errors gracefully.
  - Maintain a global state to persist information across executions.
*/
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class Main {

    // A global state map to store variables or data across executions.
    private static Map<String, Object> globalState = new HashMap<>();

    // Main method:
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner to read user input

        // Welcome message
        System.out.println("Welcome to Swift Interpreter!");
        System.out.println("Enter your code (type 'exit' on a new line to quit):");

        StringBuilder codeBuilder = new StringBuilder(); // Accumulates multi-line code
        String line; // Stores the current line of input
        SwiftInterpreter interpreter = new SwiftInterpreter(List.of()); // Initialize interpreter with empty tokens

        // This is a loop to handle the interactive prompt
        while (true) {
            System.out.print("> "); // Prompt symbol
            line = scanner.nextLine().trim(); // Read and trim the input

            // Check if the user wants to exit
            if (line.equalsIgnoreCase("exit")) {
                break;
            }

            if (!line.isEmpty()) { // Ignore empty lines
                codeBuilder.append(line).append("\n"); // Append input to the code buffer

                // Check if the accumulated code is complete
                if (isCompleteCode(codeBuilder.toString())) {
                    try {
                        // Attempt to execute the complete code
                        executeCode(codeBuilder.toString(), interpreter);
                        codeBuilder = new StringBuilder(); // Clear the buffer after successful execution
                    } catch (Exception e) {
                        // Handle errors and display the error message
                        System.err.println("Error: " + e.getMessage());
                        codeBuilder = new StringBuilder(); // Clear the buffer to avoid cascading errors
                    }
                }
            }
        }

        // Exit message
        System.out.println("Goodbye!");
        scanner.close(); // Close the scanner
    }

    // This is a method that determines if the provided code is complete.
    // It checks for matching braces and verifies that the last line does not indicate an incomplete statement.
    // The parameter "String code" is the accumulated code to check.
    //  As a result, the method returns True if the code is complete and returns false otherwise.
    private static boolean isCompleteCode(String code) {
        int braces = 0; // Tracks the balance of curly braces
        String[] lines = code.split("\n"); // Split code into lines

        for (String line : lines) {
            line = line.trim(); // Trim whitespace from each line
            for (char c : line.toCharArray()) {
                if (c == '{') braces++; // Increment for an opening brace
                if (c == '}') braces--; // Decrement for a closing brace
            }
        }

        // If braces are unbalanced, the code is incomplete
        if (braces != 0) return false;

        // Check if the last line indicates an incomplete block
        String lastLine = lines[lines.length - 1].trim();
        return !lastLine.endsWith("{") &&
                !lastLine.startsWith("if") &&
                !lastLine.startsWith("while") &&
                !lastLine.startsWith("else");
    }


    // This method executes the provided code using the SwiftInterpreter.
    // It tokenizes the code, updates the interpreter with the tokens, and runs the execution
    // The parameter  "String code" is the Swift-like code to execute.
    // The parameter "SwiftInterpreter interpreter" is the SwiftInterpreter instance used for execution.
    // This method throws RuntimeException If an error occurs during execution.
    private static void executeCode(String code, SwiftInterpreter interpreter) {
        try {
            // Create a tokenizer to tokenize the input code
            Tokenizer tokenizer = new Tokenizer(code);
            List<Token> tokens = tokenizer.tokenize(); // Generate tokens from the code

            // Update the interpreter with the new tokens, preserving its state
            interpreter.updateTokens(tokens);
            interpreter.execute(); // Execute the code
        } catch (Exception e) {
            // Rethrow the exception as a runtime exception with its message
            throw new RuntimeException(e.getMessage());
        }
    }
}