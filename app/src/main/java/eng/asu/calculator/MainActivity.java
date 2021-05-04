package eng.asu.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView resultTextView;
    TextView historyTextView;
    boolean dotted = false; //This flag is used so that a user can't put more than a dot in a number
    boolean isNumber = true; //This flag is used to find out if the lst pushed button was a number or not
    boolean equalled = false; //Flag used t determine if the last button pushed was equal
    String op; //Contains last chosen operation
    double firstNumber; //Contains the first number in the equation
    double memory = 0; //Contatins the number stored by memory calls
    DecimalFormat df = new DecimalFormat("#.####"); //Used for 4 digits precision

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.result);
        historyTextView = findViewById(R.id.history);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * This function is used to put the double number in the right format, small numbers focus on precisions
     * while big ones focus on scientific notations
     * @param d
     * @return
     */
    public String fromDoubleToString(double d) {
        if(d < 100000000.0) //If number is greater than this, it's switched to scientific notation, so there's no need for the 4 decimal precision
            return df.format(d);
        else
            return String.valueOf(d);
    }

    /**
     * This function handles all memory calls functions
     * @param v
     */
    public void memoryClick(View v){
        Button b = (Button)v;
        String memBtn = b.getText().toString();
        double number = Double.parseDouble(resultTextView.getText().toString());
        switch(memBtn)
        {
            case "M+":
                memory += number;
                break;
            case "M-":
                memory -= number;
                break;
            case "MC":
                memory = 0;
                break;
            case "MR":
                resultTextView.setText(String.valueOf(memory));
                isNumber = false;
                break;
        }
    }

    /**
     * Function used to handle pressing of number buttons and the dot
     * @param v
     */
    public void numberClick(View v){
        Button b = (Button)v;
        equalReset();
        if(!isNumber || equalled) //Ensures no concatenation is done after an operation was over, ensures the start of a new number
            resultTextView.setText("0");
        if(b.getText().toString().equals("."))
        {
            if(!dotted) { //To prevent multiple dots
                resultTextView.append(b.getText().toString());
                dotted = true;
            }
        }
        else if(resultTextView.getText().toString().equals("0")){
            if(!b.getText().toString().equals("0")) { //For zero suppression
                resultTextView.setText(b.getText().toString());
            }
        }
        else
        {
            resultTextView.append(b.getText().toString());
        }
        isNumber = true;
    }

    /**
     * Function used to handle the pressing of delete and C buttons
     * @param v
     */
    public void deleteClick(View v){
        Button b = (Button)v;
        if(b.getText().toString().equals("C")) { //On C reset the calculator
            resultTextView.setText("0");
            historyTextView.setText("");
            dotted = false;
            isNumber = false;
            equalled = false;
            op = "";
            firstNumber = 0;
        }
        else {
            equalReset();
            stringReset();
            if (resultTextView.getText().toString().length() == 1) //If 1 number is to be deleted then it becomes 0 instead
                resultTextView.setText("0");
            else if (!resultTextView.getText().toString().equals("0")) {
                String num = resultTextView.getText().toString();
                num = num.substring(0, num.length() - 1);
                resultTextView.setText(num);
            }
        }
    }

    /**
     * This function is used to handle the +ve -ve sign button, to switch signs
     * @param v
     */
    public void signClick(View v){
        equalReset();
        stringReset();
        double number = Double.parseDouble(resultTextView.getText().toString());
        number = -number;
        resultTextView.setText(fromDoubleToString(number));
    }

    /**
     * This function handles the click on percentage method, which takes the imput number and divides it by 100
     * @param v
     */
    public void percentClick(View v)
    {
        double resultVal = Double.parseDouble(resultTextView.getText().toString());
        resultTextView.setText(fromDoubleToString(resultVal/100.0));
    }

    /**
     * This is the function which handles the operation buttons click
     * @param v
     */
    public void operationClick(View v) {
        Button b = (Button) v;
        stringReset();
        dotted = false;
        op = b.getText().toString();
        String historyText = historyTextView.getText().toString();
        double resultVal = Double.parseDouble(resultTextView.getText().toString());
        double resultant = 0;
        String toBeAppended = " " + op + " ";
        char oldOp =' ';
        if(historyText.equals("") || equalled) {
            equalReset();
            resultant = resultVal;
            historyTextView.setText(fromDoubleToString(resultant) + toBeAppended);
            firstNumber = resultant;
        }
        else if(!isNumber)
        {
            historyTextView.setText(fromDoubleToString(firstNumber) + toBeAppended);
        }
        else {
            oldOp = historyText.charAt(historyText.length() - 2);
            resultant = calculate(resultVal,oldOp+"");
            historyTextView.setText(fromDoubleToString(resultant) + toBeAppended);
            firstNumber = resultant;
        }

        isNumber = false;
    }

    /**
     * This function handles the click of the equal button
     * @param v
     */
    public void equalClick(View v){
        double resultVal = 0;
        dotted = false;
        if(!resultTextView.getText().toString().equals("Undefined")) { //This condition is here to prevent another equal click after the result was undefined
            resultVal = Double.parseDouble(resultTextView.getText().toString());
            double resultant = 0;
            if (equalled) { //Keep repeating last operation in case of multiple equal presses
                resultant = calculate(resultVal, op);
                historyTextView.setText(fromDoubleToString(resultant) + " " + op + " " + fromDoubleToString(resultVal) + " =");
            } else if (!historyTextView.getText().toString().equals("")) { //Condition exists so that an equal without operation does nothing
                resultant = calculate(resultVal, op);
                equalled = true;
                historyTextView.append(fromDoubleToString(resultVal) + " =");
            }
            firstNumber = resultant;
            isNumber = false;
        }
    }

    /**
     * This function is used to reset the history text in case the last button pressed was equal
     */
    public void equalReset()
    {
        if(equalled) {
            historyTextView.setText("");
            equalled = false;
        }
    }

    /**
     * This function is used to reset the result view if the last output was infinity or undefined
     */
    public void stringReset(){
        if(resultTextView.getText().toString().equals("Undefined") || resultTextView.getText().toString().equals("Infinity"))
            resultTextView.setText("0");
    }

    /**
     * This is the function that handles the calculations in case an equal was pressed or a 2nd or more operation was used
     * @param resultVal this is the value of the number in the result text
     * @param operation the operation to be done
     * @return
     */
    public double calculate(double resultVal, String operation){
        double resultant = 0;
        switch (operation){
            case "+":
                resultant = firstNumber + resultVal;
                resultTextView.setText(fromDoubleToString(resultant));
                break;
            case "-":
                resultant = firstNumber - resultVal;
                resultTextView.setText(fromDoubleToString(resultant));
                break;
            case "×":
                resultant = firstNumber * resultVal;
                resultTextView.setText(fromDoubleToString(resultant));
                break;
            case "÷":
                if (resultVal == 0) {
                    historyTextView.setText("");
                    resultTextView.setText("Undefined");
                    dotted = false;
                    isNumber = false;
                    equalled = true;
                    firstNumber = 0;
                    op = "";
                } else {
                    resultant = firstNumber / resultVal;
                    resultTextView.setText(fromDoubleToString(resultant));
                }
                break;
        }
        return resultant;
    }
}
