package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.EmptyStackException;
import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;
    private TextView tvHistory;

    private StringBuilder expression = new StringBuilder();
    private String lastResult = "";
    private boolean isResultDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        initViews();
        setupButtons();
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tv_expression);
        tvResult = findViewById(R.id.tv_result);
        tvHistory = findViewById(R.id.tv_history);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("计算器");
        }
    }

    private void setupButtons() {
        // 数字按钮
        int[] numberIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
                R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
                R.id.btn_8, R.id.btn_9};

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(v -> onNumberClick(((Button) v).getText().toString()));
        }

        // 操作符按钮
        findViewById(R.id.btn_add).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> onOperatorClick("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperatorClick("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> onOperatorClick("÷"));

        // 其他按钮
        findViewById(R.id.btn_decimal).setOnClickListener(v -> onDecimalClick());
        findViewById(R.id.btn_left_paren).setOnClickListener(v -> appendToExpression("("));
        findViewById(R.id.btn_right_paren).setOnClickListener(v -> appendToExpression(")"));
        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsClick());
        findViewById(R.id.btn_negate).setOnClickListener(v -> onNegateClick());

        // 清除按钮
        Button btnC = findViewById(R.id.btn_clear);
        Button btnCE = findViewById(R.id.btn_clear_entry);

        btnC.setOnClickListener(v -> onClearClick());
        btnC.setOnLongClickListener(v -> {
            onClearAll();
            return true;
        });

        btnCE.setOnClickListener(v -> onClearEntryClick());
        btnCE.setOnLongClickListener(v -> {
            onClearAll();
            return true;
        });
    }

    private void onNumberClick(String number) {
        if (isResultDisplayed) {
            expression.setLength(0);
            isResultDisplayed = false;
        }
        appendToExpression(number);
    }

    private void onOperatorClick(String operator) {
        if (isResultDisplayed && !lastResult.isEmpty()) {
            expression.setLength(0);
            expression.append(lastResult);
            isResultDisplayed = false;
        }

        if (expression.length() > 0) {
            char lastChar = expression.charAt(expression.length() - 1);
            if (isOperator(lastChar)) {
                expression.setCharAt(expression.length() - 1, operator.charAt(0));
            } else {
                appendToExpression(operator);
            }
        }
    }

    private void onDecimalClick() {
        if (isResultDisplayed) {
            expression.setLength(0);
            appendToExpression("0.");
            isResultDisplayed = false;
            return;
        }

        // 检查当前数字是否已有小数点
        int lastOperatorIndex = expression.length() - 1;
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i);
            if (c == '+' || c == '-' || c == '×' || c == '÷' || c == '(') {
                lastOperatorIndex = i;
                break;
            }
            if (c == '.') return;
        }

        String currentNumber = expression.substring(lastOperatorIndex + 1);
        if (!currentNumber.contains(".")) {
            if (currentNumber.isEmpty()) {
                appendToExpression("0.");
            } else {
                appendToExpression(".");
            }
        }
    }

    private void onEqualsClick() {
        if (expression.length() == 0) return;

        try {
            String expr = expression.toString();
            // 替换显示符号为计算符号
            expr = expr.replace('×', '*').replace('÷', '/');
            double result = evaluateExpression(expr);

            lastResult = formatResult(result);
            tvHistory.setText(expression.toString() + " =");
            tvResult.setText(lastResult);
            isResultDisplayed = true;
        } catch (Exception e) {
            tvResult.setText("错误");
            Toast.makeText(this, "表达式错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void onClearClick() {
        if (expression.length() > 0) {
            expression.setLength(0);
            tvExpression.setText("0");
        }
    }

    private void onClearEntryClick() {
        if (expression.length() > 0) {
            expression.setLength(0);
            tvExpression.setText("0");
            tvResult.setText("0");
        }
    }

    private void onClearAll() {
        expression.setLength(0);
        lastResult = "";
        tvExpression.setText("0");
        tvResult.setText("0");
        tvHistory.setText("");
        isResultDisplayed = false;
    }

    private void onNegateClick() {
        if (expression.length() == 0) return;

        // 找到最后一个数字的位置
        int lastNumStart = expression.length() - 1;
        for (int i = expression.length() - 2; i >= 0; i--) {
            char c = expression.charAt(i);
            if (isOperator(c) || c == '(') {
                lastNumStart = i + 1;
                break;
            }
            if (i == 0) lastNumStart = 0;
        }

        String lastNum = expression.substring(lastNumStart);
        String prefix = expression.substring(0, lastNumStart);

        // 如果已经是负数，去掉负号
        if (lastNum.startsWith("-")) {
            expression.setLength(lastNumStart);
            expression.append(lastNum.substring(1));
        } else if (lastNumStart > 0 || expression.charAt(lastNumStart) != '0') {
            // 在数字前加负号
            expression.setLength(lastNumStart);
            expression.append("-").append(lastNum);
        }

        updateDisplay();
    }

    private void appendToExpression(String str) {
        if (expression.length() == 0 && str.equals("×")) return;
        if (expression.length() == 0 && str.equals("÷")) return;

        expression.append(str);
        updateDisplay();
    }

    private void updateDisplay() {
        String expr = expression.toString();
        if (expr.isEmpty()) {
            tvExpression.setText("0");
        } else {
            tvExpression.setText(expr);
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            String str = String.valueOf(result);
            // 限制小数位数
            if (str.length() > 15) {
                str = String.format("%.10f", result);
                // 去掉末尾的0
                str = str.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
            return str;
        }
    }

    // 简单表达式解析器
    private double evaluateExpression(String expr) throws Exception {
        expr = expr.trim();
        if (expr.isEmpty()) {
            throw new EmptyStackException();
        }

        return parseExpression(expr);
    }

    private double parseExpression(String expr) throws Exception {
        int parenCount = 0;
        int lastOpPos = -1;

        // 从右往左找最低优先级的操作符（考虑括号）
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);

            if (c == ')') parenCount++;
            else if (c == '(') parenCount--;

            if (parenCount == 0 && (c == '+' || c == '-') && i > 0) {
                // 找到加或减（不是负号）
                char prev = expr.charAt(i - 1);
                if (!isOperator(prev) && prev != '(') {
                    lastOpPos = i;
                    break;
                }
            }
        }

        if (lastOpPos == -1) {
            // 没有找到加或减，找乘除
            parenCount = 0;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);

                if (c == ')') parenCount++;
                else if (c == '(') parenCount--;

                if (parenCount == 0 && (c == '*' || c == '/')) {
                    lastOpPos = i;
                    break;
                }
            }
        }

        if (lastOpPos == -1) {
            // 没有找到操作符，检查是否有括号
            if (expr.startsWith("(") && expr.endsWith(")")) {
                return parseExpression(expr.substring(1, expr.length() - 1));
            }
            // 否则是单个数字
            return parseNumber(expr);
        }

        char op = expr.charAt(lastOpPos);
        String left = expr.substring(0, lastOpPos);
        String right = expr.substring(lastOpPos + 1);

        double leftVal = parseExpression(left);
        double rightVal = parseExpression(right);

        switch (op) {
            case '+':
                return leftVal + rightVal;
            case '-':
                return leftVal - rightVal;
            case '*':
                return leftVal * rightVal;
            case '/':
                if (rightVal == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftVal / rightVal;
            default:
                throw new Exception("Unknown operator");
        }
    }

    private double parseNumber(String numStr) throws NumberFormatException {
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number: " + numStr);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}