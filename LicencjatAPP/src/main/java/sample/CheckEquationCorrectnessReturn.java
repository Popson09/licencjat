package sample;

import java.util.ArrayList;
import java.util.List;

public class CheckEquationCorrectnessReturn {
    String message="";
    List<String> equations=new ArrayList<>();;
    boolean isCorrect=false;

    public CheckEquationCorrectnessReturn(String message, List<String> equations, boolean isCorrect) {
        this.message = message;
        this.equations = equations;
        this.isCorrect = isCorrect;
    }
}
