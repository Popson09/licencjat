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
    public void showAlgebraFile()
    {
        System.out.println("algebraName: "+algName);
        System.out.println("cardinalityValue: "+cardinality);
        System.out.println("operations:" );
        for (Operation operation : operations) {
            System.out.println("---------------------");
            System.out.println("opName: "+operation.getOpName());
            System.out.println("opArity: "+operation.getArity());
            operation.showOpTableValue();
        }
    }
    void clear()
    {
        
        this.operations.clear();
        this.isLoad=false;
        this.algName="";
        this.cardinality=0;

    }
}
