package sample;

import java.util.ArrayList;
import java.util.List;

public class Algebra {
    private String algName;
    private int cardinality; //numer w tablicy odpowiada zapisowi w systenie reprezentowanym przez cardinality
    private final List<Operation> operations;
    public boolean isLoad;

    public Algebra() {

        this.algName="";
        this.cardinality=0;
        this.operations=new ArrayList<Operation>();
        this.isLoad=false;
    }

    public String getAlgName() {
        return algName;
    }

    public void setAlgName(String algName) {
        this.algName = algName;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = Integer.parseInt(cardinality);
    }
    public void setOperationsValue(int index,Operation value)
    {
        this.operations.add(index,value);
    }

    public List<Operation> getOperations() {
        return operations;
    }
    public String showAlgebraFile()
    {
        StringBuilder s= new StringBuilder();
        s.append("algebraName: ").append(algName).append('\n');
        s.append("cardinalityValue: ").append(cardinality).append('\n');
        s.append("operations:\n");
        for (Operation operation : operations) {
            s.append("---------------------\n");
            s.append("opName: ").append(operation.getOpName()).append('\n');
            s.append("opArity: ").append(operation.getArity()).append('\n');
            s.append(operation.showOpTableValue(getCardinality()));
        }
        return s.toString();
    }
    void clear()
    {

        this.operations.clear();
        this.isLoad=false;
        this.algName="";
        this.cardinality=0;

    }
}
