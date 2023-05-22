package sample;

import java.util.ArrayList;
import java.util.List;

public class EquationTable {
    private String opName;
    private List<String> variables;
    private int arity;
    private String result;



    EquationTable( int w) {
        this.opName = "";
        this.variables = new ArrayList<>();
        this.arity = 0;
        this.result = "W"+w;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }
    public int getArity() {
        return arity;
    }

    public void setArity(int arity) {
        this.arity = arity;
    }
    public void clear()
    {
        this.opName="";
        this.variables.clear();
        this.arity=0;
        this.result="";
    }
}
