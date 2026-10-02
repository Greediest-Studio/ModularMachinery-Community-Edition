package github.kasuminova.mmce.client.gui.util;

/** 数量输入表达式：四则运算、右结合幂、括号与科学计数法。 */
public final class QuantityExpression {
    private final String expression;
    private int position;

    private QuantityExpression(String expression) {
        this.expression = expression;
    }

    public static double parse(String expression) {
        QuantityExpression parser = new QuantityExpression(expression);
        double result = parser.sum();
        parser.skipWhitespace();
        if (parser.position != expression.length() || !Double.isFinite(result)) {
            throw new NumberFormatException("Invalid quantity expression");
        }
        return result;
    }

    private double sum() {
        double value = product();
        while (true) {
            if (take('+')) value += product();
            else if (take('-')) value -= product();
            else return value;
        }
    }

    private double product() {
        double value = unary();
        while (true) {
            if (take('*')) value *= unary();
            else if (take('/')) value /= unary();
            else return value;
        }
    }

    private double unary() {
        if (take('+')) return unary();
        if (take('-')) return -unary();
        double value = primary();
        // 2^3^2 = 2^(3^2)，同时允许 2^-2 和 -(2^2)。
        return take('^') ? Math.pow(value, unary()) : value;
    }

    private double primary() {
        if (take('(')) {
            double value = sum();
            if (!take(')')) throw new NumberFormatException("Missing closing parenthesis");
            return value;
        }
        skipWhitespace();
        int start = position;
        while (position < expression.length()) {
            char ch = expression.charAt(position);
            if (!Character.isDigit(ch) && ch != '.') break;
            position++;
        }
        if (position < expression.length()
            && (expression.charAt(position) == 'e' || expression.charAt(position) == 'E')) {
            position++;
            if (position < expression.length()
                && (expression.charAt(position) == '+' || expression.charAt(position) == '-')) position++;
            while (position < expression.length() && Character.isDigit(expression.charAt(position))) position++;
        }
        return Double.parseDouble(expression.substring(start, position));
    }

    private boolean take(char expected) {
        skipWhitespace();
        if (position < expression.length() && expression.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private void skipWhitespace() {
        while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) position++;
    }
}
