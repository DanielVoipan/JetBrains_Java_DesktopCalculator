package calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

import static calculator.Calculator.op.*;

public class Calculator extends JFrame {

    private final JLabel resultLabel;
    private final JLabel equationLabel;

    private final JPanel buttonPanel;


    {
        resultLabel = new JLabel("0");
        equationLabel = new JLabel(" ");
        buttonPanel = new JPanel(new GridLayout(6, 4, 10, 10));
    }

    enum op {
        Multiply("\u00D7", "*", 2),
        Divide("\u00F7", "/", 2),
        Add("\u002B", "+", 1),
        Subtract("-", "-", 1),
        SquareRoot("\u221A", "square", 1),
        PowerTwo("X\u00b2","power2", 3),
        PowerY("X\u02B8", "XY", 0),
        PlusMinus("\u00b1", "+-", 0);

        final String symbolUtf8;
        final String symbol;
        final int precedence;

        op(String symbolUtf8, String symbol, int precedence) {
            this.symbol = symbol;
            this.symbolUtf8 = symbolUtf8;
            this.precedence = precedence;
        }

        // get utf8 string of the operator
        static String getUtfSymbol(String symbol) {
            for (op o : op.values()) {
                if (o.symbol.equals(symbol)) {
                    return o.symbolUtf8;
                }
            }
            return symbol;
        }

        // get the symbol of the operator
        static String getSymbol(String symbol) {
            for (op o : op.values()) {
                if (o.symbolUtf8.equals(symbol)) {
                    return o.symbol;
                }
            }
            return null;
        }

        static char getCharSymbol(char symbol) {
            for (op o : op.values()) {
                if (o.symbolUtf8.equals(String.valueOf(symbol))) {
                    return o.symbol.charAt(0);
                }
            }
            return symbol;
        }

        // check if valid operator symbol
        static boolean validSymbol(char utf8Symbol) {
            String symbol = String.valueOf(utf8Symbol);
            for (op o : op.values()) {
                if (o.symbolUtf8.equals(symbol)) {
                    return true;
                }
            }
            return false;
        }

        // get the precedence of the operator
        static int getPrecedence(String utf8Symbol) {
            for (op o : op.values()) {
                if (o.symbolUtf8.equals(utf8Symbol)) {
                    return o.precedence;
                }
            }
            return 0;
        }
    }

    enum calc {
        Parentheses("( )"),
        CE("CE"),
        Clear("C"),
        Delete("Del"),
        PowerTwo("power2"),
        PowerY("XY"),
        SquareRoot("square"),
        Divide("/"),
        Seven("7"),
        Eight("8"),
        Nine("9"),
        Multiply("*"),
        Four("4"),
        Five("5"),
        Six("6"),
        Subtract("-"),
        One("1"),
        Two("2"),
        Three("3"),
        Add("+"),
        PlusMinus("+-"),
        Zero("0"),
        Dot("."),
        Equals("=");

        final String symbol;

        calc(String s) {
            symbol = s;
        }
    }

    public Calculator() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(325, 540);
        setTitle("Calculator");
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setLocationRelativeTo(null);
        setResizable(false);

        GridBagConstraints c = new GridBagConstraints();
        JPanel resultsPanel = new JPanel(new GridBagLayout());
        Font resultFont = new Font(resultLabel.getFont().getName(), resultLabel.getFont().BOLD, 40);
        resultLabel.setName("ResultLabel");
        resultLabel.setFont(resultFont);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(15,0, 15, 0));
        c.gridx=0;
        c.gridy=0;
        c.insets = new Insets(0,285,0,0);  //left padding
        resultsPanel.add(resultLabel, c);
        add(resultsPanel);

        JPanel equationPanel = new JPanel(new GridBagLayout());
        Font equationFont = new Font(equationLabel.getFont().getName(), equationLabel.getFont().getStyle(), 16);
        equationLabel.setName("EquationLabel");
        equationLabel.setFont(equationFont);
        equationLabel.setForeground(Color.green.darker());
        equationLabel.setBorder(BorderFactory.createEmptyBorder(0,0,35,0));
        c.gridx=0;
        c.gridy=1;
        c.insets = new Insets(0,270,0,0);  //left padding
        equationPanel.add(equationLabel, c);
        add(equationPanel);

        // add calculator buttons
        addButtons();
        // add second row
        add(buttonPanel);
        setVisible(true);
    }

    // add calculator buttons
    void addButtons() {
        JButton button;
        for (calc s : calc.values()) {
            button = new JButton(getUtfSymbol(s.symbol));
            button.setName(s.name());
            button.setPreferredSize(new Dimension(70,40));
            button.addActionListener(this::actionPerformed);
            button.setActionCommand(s.symbol);
            buttonPanel.add(button);
        }
    }

    // action performed for the buttons
    public void actionPerformed(ActionEvent e) {
        String command = ((JButton) e.getSource()).getActionCommand();
        String text;
        text = getUtfSymbol(command);
        switch (command) {
            case "+-" -> negate();
            case "XY" -> addPowerY();
            case "power2" -> addPower2();
            case "square" -> addSquare(text);
            case "( )" -> addParentheses();
            case "=" -> evaluate();
            case "Del" -> deleteCharacter();
            case "C" -> {
                equationLabel.setText(" ");
                equationLabel.setForeground(Color.green.darker());
                resultLabel.setText("0");
            }
            default -> {
                if (equationLabel.getText().equals(" ")) {
                    // not allow equation to start with an operator
                    if (validSymbol(text.charAt(0))) {
                        return;
                    }
                    equationLabel.setText(text);
                } else {
                    // add 0. , at start if a digit starts with .
                    boolean startWithDot = addZeroIfDot(text);
                    if (startWithDot) {
                        return;
                    }
                    // if two operators are inserted consecutively, then
                    // the second operator should replace the first
                    String replaceOp = replaceOperator(text);
                    if (!Objects.equals(replaceOp, null)) {
                        equationLabel.setText(replaceOp);
                    } else {
                        equationLabel.setText(equationLabel.getText() + text);
                    }
                }
            }
        }
    }

    // evaluate expression with exceptions

    void evaluate() {
        String text = equationLabel.getText();
        if (validSymbol(text.charAt(text.length() - 1)) && !getSymbol(String.valueOf(text.charAt(text.length() - 1))).equals("square")) {
            equationLabel.setForeground(Color.red.darker());
            return;
        }
        if (text.charAt(text.length() - 1) == '0' && String.valueOf(text.charAt(text.length() - 2)).equals(op.getUtfSymbol("/"))) {
            equationLabel.setForeground(Color.red.darker());
            return;
        }
        if (text.charAt(text.length() - 1) == '(') {
            equationLabel.setForeground(Color.red.darker());
            return;
        }
        if (text.charAt(text.length() - 1) == ')' && text.charAt(text.length() - 1) == '(') {
            equationLabel.setForeground(Color.red.darker());
            return;
        }
        resultLabel.setText(String.valueOf(eval()));
    }

    // Negate number or expression

    void negate() {
        String lastText = equationLabel.getText();
        if (!lastText.equals(" ")) {
            if (lastText.charAt(lastText.length() - 1) == '-' && lastText.charAt(lastText.length() - 2) == '(') {
                String newText = lastText.substring(0, lastText.length() - 2);
                equationLabel.setText(newText + " ");
            } else if (validSymbol(lastText.charAt(lastText.length() - 1))) {
                equationLabel.setText(lastText + "(-");
            } else if (String.valueOf(lastText.charAt(lastText.length() - 1)).matches("[0-9]")) {
                char lastNum = lastText.charAt(lastText.length() - 1);
                if (lastText.length() == 3) {
                    if (lastText.charAt(lastText.length() - 2) == '-' && lastText.charAt(0) == '(') {
                        String newText = lastText.substring(0, 0);
                        newText = newText + lastNum;
                        equationLabel.setText(newText);
                    } else {
                        String newText = lastText.substring(0, lastText.length() - 1);
                        equationLabel.setText(newText + "(-" + lastNum);
                    }
                } else {
                    String newText = lastText.substring(0, lastText.length() - 1);
                    equationLabel.setText(newText + "(-" + lastNum);
                }
            }
        } else {
            equationLabel.setText("(-");
        }
    }

    // add Power Of Two

    void addPower2() {
        String lastText = equationLabel.getText();
        if (!String.valueOf(lastText.charAt(lastText.length() - 1)).matches("[0-9]") && lastText.charAt(lastText.length() - 1) != ')') {
            return;
        }
        equationLabel.setText(lastText + '^' + "(2)");
    }

    // add Power of Y, where Y is specified.
    void addPowerY() {
        String lastText = equationLabel.getText();
        if (!String.valueOf(lastText.charAt(lastText.length() - 1)).matches("[0-9]") && lastText.charAt(lastText.length() - 1) != ')') {
            return;
        }
        equationLabel.setText(lastText + '^' + "(");
    }

    // add Square
    void addSquare(String text) {
        String lastText = equationLabel.getText();
        equationLabel.setText(lastText + text + '(');
    }

    // add Parentheses
    void addParentheses() {
        String lastText = equationLabel.getText();
        int lcount = 0;
        int rcount = 0;
        for (int i = 0; i < lastText.length(); i++) {
            if (lastText.charAt(i) == '(')
                lcount++;
            if (lastText.charAt(i) == ')')
                rcount++;
        }
        if (lcount == rcount) {
            equationLabel.setText(lastText + '(');
        } else if (lastText.charAt(lastText.length() - 1) == '(') {
            equationLabel.setText(lastText + '(');
        } else if (validSymbol(lastText.charAt(lastText.length() - 1))) {
            equationLabel.setText(lastText + '(');
        } else {
            equationLabel.setText(lastText + ')');
        }
    }


    // add 0 before or after .
    boolean addZeroIfDot(String text) {
        String lastText = equationLabel.getText();
        String s = String.valueOf(lastText.charAt(lastText.length() - 1));
        if (s.equals(calc.Dot.symbol)) {
            assert text != null;
            if (lastText.length() > 1 && String.valueOf(lastText.charAt(lastText.length() - 2)).matches("[0-9]")) {
                if (validSymbol(text.charAt(0))) {
                    String t = lastText.substring(0, lastText.length() - 1);
                    equationLabel.setText(t + ".0" + text);
                    return true;
                }
            }
            if (lastText.length() > 1 && text.matches("[0-9]") && String.valueOf(lastText.charAt(lastText.length() - 2)).matches("[0-9]")) {
                equationLabel.setText(equationLabel.getText() + text);
                return true;
            } else if (text.matches("[0-9]")) {
                lastText = lastText.replace(lastText.charAt(lastText.length() - 1), '0');
                String t = "." + text;
                equationLabel.setText(lastText + t);
                return true;
            }
        }
        return false;
    }

    // replace operator if two operators are inserted consecutively
    String replaceOperator(String text) {
        String equation = equationLabel.getText();
        char lastOp = equation.charAt(equation.length() - 1);
        if (validSymbol(lastOp) && validSymbol(text.charAt(0))) {
            return equation.replace(equation.charAt(equation.length() - 1), text.charAt(0));
        } else {
            return null;
        }
    }

    // delete characters starting from end to start
    void deleteCharacter() {
        if (!equationLabel.getText().equals(" ")) {
            String text = equationLabel.getText();
            String newText = text.substring(0, text.length() - 1);
            if (newText.equals("")) {
                equationLabel.setText(" ");
            } else {
                equationLabel.setText(newText);
            }
        }
        equationLabel.setForeground(Color.green.darker());
    }

    // equation evaluation
    BigDecimal eval() {
        String str = equationLabel.getText();
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? getCharSymbol(str.charAt(pos)) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            BigDecimal parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);

                BigDecimal result = new BigDecimal(x);
                if (result.scale() > 0) {
                    String[] removeTrailingZero = result.toString().split("\\.");
                    if (removeTrailingZero[1].equals("0") || removeTrailingZero[1].equals("00")) {
                        return result.setScale(0);
                    } else {
                        return result.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
                    }
                }
                return result;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = getSymbol(str.substring(startPos, this.pos));
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("square")) x = Math.sqrt(x);
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                return x;
            }
        }.parse();
    }
}
