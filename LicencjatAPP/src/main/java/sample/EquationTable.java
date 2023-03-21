package sample;

import java.util.ArrayList;
import java.util.List;

public class EquationTable {
    private String opName;
    private List<String> variables;
    private int arity;
    public int getArity() {
        return arity;
    }

    public void setArity(int arity) {
        this.arity = arity;
    }


    EquationTable()
    {
        this.opName="";
        this.variables=new ArrayList<>();
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
    public void clear()
    {
        this.opName="";
        this.variables.clear();
    }
}
