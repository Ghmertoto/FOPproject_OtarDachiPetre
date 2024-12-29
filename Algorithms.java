import java.util.List;

// InterpreterTest is the class used to test different Swift code examples and execute them using a Swift interpreter
public class Algorithms{
    public static void main(String[] args) {
        // Test cases for different functionalities
        sumOfFirstNumbers();
        System.out.println("--------------------------");
        factorialOfNumber();
        System.out.println("--------------------------");
        gcd();
        System.out.println("--------------------------");
        reverseNumber();
        System.out.println("--------------------------");
        isPalindrome();
        System.out.println("--------------------------");
        fibonacci();
        System.out.println("--------------------------");
        multable();
        System.out.println("--------------------------");
        sumDigits();
        System.out.println("--------------------------");
        findbigdigit();
        System.out.println("--------------------------");
        isPrime();
        System.out.println("--------------------------");
    }

    // executeCode executes the provided Swift code by tokenizing it and passing the tokens to the SwiftInterpreter
    private static void executeCode(String sourceCode) {
        System.out.println("Executing code:\n" + sourceCode + "\n");
        try {
            // Tokenizing the source code
            Tokenizer tokenizer = new Tokenizer(sourceCode);
            List<Token> tokens = tokenizer.tokenize();

            // Passing tokens to SwiftInterpreter for execution
            SwiftInterpreter interpreter = new SwiftInterpreter(tokens);
            interpreter.execute();
        } catch (Exception e) {
            // Handling errors during execution
            System.err.println("Execution failed: " + e.getMessage());
        }
    }

    // sumOfFirstNumbers tests the code for calculating the sum of first N numbers
    private static void sumOfFirstNumbers() {
        System.out.println("sum of first N numbers: ");
        // Swift code for summing first N numbers
        String code = """
        var sum = 0
        var n = 10
        var i = 1
        while i <= n {
            sum = sum + i
            i = i + 1
        }
        print(sum)""";
        executeCode(code);
    }

    // factorialOfNumber tests the code for calculating the factorial of a number
    private static void factorialOfNumber() {
        System.out.println("Factorial of a number: ");
        // Swift code for calculating factorial
        String code = """
                var n = 5
                var factorial = 1
                var i = 1
                while i <= n {
                    factorial = factorial * i
                    i = i + 1
                }
                print(factorial)
                """;
        executeCode(code);
    }

    // gcd tests the code for calculating the greatest common divisor (GCD)
    private static void gcd() {
        System.out.println("gcd: ");
        // Swift code for calculating the GCD of two numbers
        String code = """
        var x = 56
        var y = 98
        while y != 0 {
        var remainder = x % y
        x = y
        y = remainder}
        if x < 0 {
        x = -x}
        print(x)""";
        executeCode(code);
    }

    // reverseNumber tests the code for reversing a number
    private static void reverseNumber() {
        System.out.println("reverseNumber: ");
        // Swift code for reversing a number
        String code = """
        var n = 56
        var reversed = 0
        while n != 0 {
        var digit = n % 10
        reversed = reversed * 10
        reversed = reversed + digit
        n = n / 10}
        print(reversed)""";
        executeCode(code);
    }

    // isPalindrome tests the code for checking if a number is a palindrome
    private static void isPalindrome() {
        System.out.println("isPalindrome: ");
        // Swift code for checking if a number is a palindrome
        String code = """
        var n = 11112
        var reversed = 0
        var original = n
        while n != 0 {
        var digit = n % 10
        reversed = reversed * 10
        reversed = reversed + digit
        n = n / 10
        }
        if original == reversed {
        print("true")} else {
        print("false")
        }""";
        executeCode(code);
    }

    // fibonacci tests the code for generating Fibonacci numbers
    private static void fibonacci() {
        System.out.println("fibonacci: ");
        // Swift code for generating the nth Fibonacci number
        String code = """
        var N = 10
        var a = 0
        var b = 1
        var count = 2
        while count < N {
        var next = a + b
        a = b
        b = next
        count = count + 1
        }
        print(b)""";
        executeCode(code);
    }

    // isPrime tests the code for checking if a number is prime
    private static void isPrime() {
        System.out.println("isPrime: ");
        // Swift code for checking if a number is prime
        String code = """
                var num = 5
                            var isPrime = 1
                           
                            if num <= 1 {
                                isPrime = 0
                            }
                           
                            var i = 2
                            while i * i <= num {
                                if num % i == 0 {
                                    isPrime = 0
                                }
                                i = i + 1
                            }
                           
                            if isPrime == 1 {
                                print(num)
                                print("yes it is prime number")
                            }
                           
                            if isPrime == 0 {
                                print(num)
                                print("no it is not prime number")
                            }
                """;
        executeCode(code);
    }

    // multable tests the code for printing the multiplication table of a number
    private static void multable() {
        System.out.println("mutable: ");
        // Swift code for printing the multiplication table of a number
        String code = """
                var number = 5
                var i = 1
                var toPrint = 0
                while i <= 10 {
                toPrint = number * i
                print(toPrint)
                i = i + 1}""";
        executeCode(code);
    }

    // sumDigits tests the code for summing the digits of a number
    private static void sumDigits() {
        System.out.println("sum digits: ");
        // Swift code for summing the digits of a number
        String code = """
                var number = 12345
                var sum = 0
                while number > 0 {
                 var digit = number % 10 
                 sum = sum + digit
                 number = number / 10
                }
                print(sum)""";
        executeCode(code);
    }

    // findbigdigit tests the code for finding the largest digit in a number
    private static void findbigdigit() {
        System.out.println("find big digit: ");
        // Swift code for finding the largest digit in a number
        String code = """
                let number = 12345
                var biggestDigit = 0
                                                
                while number > 0 {
                    var digit = number % 10
                    if digit > biggestDigit {
                        biggestDigit = digit
                    }
                    number = number / 10
                }
                                                
                print(biggestDigit)""";
        executeCode(code);
    }
}