package sample;

import java.util.ArrayList;
import java.util.List;

public class EquationTable {
    private String opName;
    private List<Integer> variables;
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

    public List<Integer> getVariables() {
        return variables;
    }

    public void setVariables(List<Integer> variables) {
        this.variables = variables;
    }
    public void clear()
    {
        this.opName="";
        this.variables.clear();
    }
}
