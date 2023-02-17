package sample;

import java.util.ArrayList;
import java.util.List;

public class Operation
{
    private String opName;
    private int arity;
    private final List <Integer>opTable;

    public Operation() {
        this.opTable=new ArrayList<Integer>();
    }
    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public int getArity() {
        return arity;
    }

    public void setArity(String arity) {
        this.arity = Integer.parseInt(arity);
    }
    public void setOpTableValue(int index,int value)
    {
        this.opTable.add(index,value);
    }
    public void showOpTableValue()
    {
        for(int i=0;i<opTable.size();i++)
        {
            System.out.println("row"+i+": "+ opTable.get(i));
        }
    }

}
