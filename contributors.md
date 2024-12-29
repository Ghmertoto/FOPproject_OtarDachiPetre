Contributors
This project was developed by a team of four members, each contributing their expertise to different components. Below is a breakdown of each member's contributions:

1. Petre
Role: Tokenizer Developer, Team support

Contributions:
Developed the Tokenizer class to tokenize Swift code.
Implemented functionality to identify keywords, values, operators, and other components of the Swift code.
Helped document the Main file for better understanding and usage.
Documented the Tokenizer functionality for clarity and ease of use.
helped Otar and declared let in interpreter

2. Otar Khunashvili
Role: Swift Interpreter Developer, Main File Developer

Contributions:
Worked on the SwiftInterpreter class, which utilizes the Tokenizer to interpret and execute Swift code.
Implemented the main file that takes user input in the form of Swift code and processes it line by line.
Collaborated with Petre to integrate the Tokenizer into the Swift Interpreter

3. Dachi
Role: Documentation and Algorithm Development

Contributions:
Wrote algorithms in Swift for testing the Tokenizer and SwiftInterpreter, ensuring that both components worked as expected.
Documented the SwiftInterpreter for clarity and understanding. (helped Otar to fix while loops)
Wrote tests for evaluating code input, ensuring the interpreter works as expected and pointing out errors in Swift code.
Supported testing of various Swift-like expressions and edge cases.

4. Demiko
Role: Documentation, Evaluation, and Team Support

Contributions:
Evaluated test results and helped ensure the accuracy of the interpreter’s output.
Responsible for writing the overall documentation, including the README and Contributors file, to provide a comprehensive overview of the project.
Assisted in documenting to ensure clarity for anyone using or extending the project.
Worked closely with the team to ensure smooth integration and mutual support throughout development.


Project Files Overview
This project consists of four main files that you can download and test on your machine:
Tokenizer.java – Tokenizes Swift-like code into components like keywords, values, operators, etc.
SwiftInterpreter.java – Interprets the tokenized code and executes it.
Main.java – Takes user input line by line, parses Swift code, and passes it to the interpreter.
InterpreterTest.java – Contains tests and pre-made algorithms for evaluating the correctness of the Swift interpreter.

How to Test
Download all four files to your local machine.
Run Main.java and input Swift code line by line.
The program will tokenize the code and execute it through the Swift interpreter.
Use the test cases in InterpreterTest.java to verify the correctness of the code.
