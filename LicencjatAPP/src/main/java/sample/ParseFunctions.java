package sample;

import java.util.*;

public class ParseFunctions {

    public static CheckEquationCorrectnessReturn checkEquationCorrectness(Algebra algebra, String equation) {
        EquationSymbols symbol;
        Queue<String> queue = new PriorityQueue<>();
        List<String> equationTable=new ArrayList<>();
        String s="";
        for (int i=0;i<equation.length();i++)
        {
            char c=equation.charAt(i);
            if((c >= 'a' && c <= 'z') || (c > 'A' && c <= 'Z')||(c>='0'&&c<='9'))
            {
                s+=Character.toString(c);
            }
            else if(c=='('||c==','||(c==')'&& i==equation.length()-1)||(c==')'&& equation.charAt(i)!=')'))
            {
                equationTable.add(s);
                s="";
            }
        }
        int opIndex = 0, opCount = 0;
        //sprawdzamy czy symbol jest operacją
        for (int i = equationTable.size()- 1; i >= 0; i--) {
            symbol = EquationSymbols.empty;
            s = equationTable.get(i);

            for (int j = 0; j < algebra.getOperations().size(); j++) {
                if (s.equals(algebra.getOperations().get(j).getOpName())) {
                    symbol = EquationSymbols.operator; //symbol znaleziony jako operator
                    opIndex = j;
                    break;
                }
            }
            if (symbol == EquationSymbols.empty) { //symbol nie jest operacją sprawdzamy czy jest zmienną
                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) && s.length() == 1) {
                        symbol = EquationSymbols.variable;//jednoliterowy string który nie jest operatorem jest zmienną
                        break;
                    } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        return new CheckEquationCorrectnessReturn("Błędne równanie: Niedozwolona nazwa zmiennej",equationTable,false);
                        // złapany jako operator to jest niewłaściwym symbolem w równaniu
                    }
                }
            }
            if (symbol == EquationSymbols.empty) {//symbol nie jest zmienną sprawdzamy czy jest stałą
                int a = Integer.parseInt(s);
                if (a >= algebra.getCardinality())
                    return new CheckEquationCorrectnessReturn("Błędne równanie: Stała poza zakresem dziedziny",equationTable,false);
                else
                    symbol = EquationSymbols.constant; //symbol jest stałą liczbową
            }
            if (symbol == EquationSymbols.constant || symbol == EquationSymbols.variable)
                queue.add(s); //dodajemy liczbe/zmienną do stosu
            else {
                for (int j = 0; j < algebra.getOperations().get(opIndex).getArity(); j++) //posiadamy symbol ściągamy odpowiednią liczbe symboli ze stosu
                {
                    if (queue.isEmpty())

                        return new CheckEquationCorrectnessReturn("Błędne równanie: Pusty stos",equationTable,false);//mamy na mało symboli na stosie czyli równanie złe
                    else {
                        queue.remove(); //ściągamy symbole, nie potrzebujemy ich znać, bo tylko sprawdzamy składnie
                    }
                }
                queue.add("w" + opCount);//dodajemy symbol wynikowy do stosu
                opCount++;
            }
        }
        if(queue.size() > 1) //na koniec w stosie powinien zostać tylko jeden symbol
            return new CheckEquationCorrectnessReturn("Błędne równanie: nadmiarowa liczba zmiennych",equationTable,false);
        return new CheckEquationCorrectnessReturn( "Równanie poprawne",equationTable,true);
    }
    public static List<EquationTable> getEquationTable(List<String> eq,Algebra algebra,int w) //na podstawie stringa wygenerowanego po sprawdzeniu poprawnosci grupuje zmnienne po operacjach
    {
        List<EquationTable> res=new ArrayList<>(); // konkretna operacja np. XOR(1,w1)
        int index=-1;
        boolean opFind;
        String s;
        for(int i=0;i<eq.size();i++)
        {
            opFind=false;
            s=eq.get(i);
            for (int j = 0; j < algebra.getOperations().size(); j++) {
                if (s.equals(algebra.getOperations().get(j).getOpName())) { //szukam nazwy operacji
                    if(index==-1) { //pierwszy argument
                        index++;
                        res.add(new EquationTable(w));
                        w++;
                        res.get(index).setOpName(s);
                        res.get(index).setArity(algebra.getOperations().get(j).getArity());
                    }
                    else { //kolejny
                        int a=index;
                        while(res.get(a).getVariables().size()==res.get(a).getArity()) //jeżeli jest to zagnieżdzona funkcja szukam jej początku
                            a--;
                        res.get(a).getVariables().add("w"+w);
                        res.add(new EquationTable(w));
                        w++;
                        index++;
                        res.get(index).setArity(algebra.getOperations().get(j).getArity());
                        res.get(index).setOpName(s);
                    }
                    opFind=true;
                    break;
                }
            }
            if(!opFind) //nie jest to operacja wpisuje po prostu zmienną
            {
                while(res.get(index).getVariables().size()==res.get(index).getArity())
                    index--;
                res.get(index).getVariables().add(s);
            }
        }
        return res;
    }
    public static void getOneFromAll(Algebra algebra,String s,CnfFileHelper cnfFileHelper) //funkcja zapewniająca dokładnie 1 wystąpienie zmiennje
    {
        String line="";
        for(int z=0;z< algebra.getCardinality();z++) //długi nawias np (x1 ||x2 ||x3) zapewniający conajmniej 1 jedynkę
        {
            cnfFileHelper.variableCode.add(s+"_"+z);
            line+=cnfFileHelper.variableCode.size();
            if(z<algebra.getCardinality()-1)
                line+=" ";
            else {
                line += " 0";
                cnfFileHelper.line.add(line);
            }
        }
        for(int z=0;z< algebra.getCardinality();z++){ //nawiasy (!x1 || !x2)&&(!x1 || !x3)&&(!x2 || !x3) zapewniające co najwyżej 1 jedynkę
            for(int q=z+1;q< algebra.getCardinality();q++) {
                cnfFileHelper.line.add("-"+ (cnfFileHelper.variableCode.indexOf(s + "_" + z)+1) +" -"+ (cnfFileHelper.variableCode.indexOf(s + "_" + q)+1)+" 0");
            }
        }
    }
    public static int decodeNumber(List<Integer> values, int cardinality) {
        int res = 0;
        for (int i = values.size() - 1, j = 0; i >= 0; i--, j++) {

            res += values.get(i) * Math.pow(cardinality, j);
        }
        return res;
    }
    public static void printAllMatches(int base, List<Integer> number, EquationTable variables, Algebra algebra,CnfFileHelper cnfFileHelper) {
        if (!number.contains(-1)) { // jeśli nie ma już niewiadomych cyfr
            int decimalNumber = decodeNumber(number, algebra.getCardinality()); // oblicz wartość liczby w systemie dziesiętnym
            char c = ' ';
            String s="";
            for (int j = 0; j < number.size(); j++) {
                c = variables.getVariables().get(j).charAt(0);
                if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                    s+="-"+(cnfFileHelper.variableCode.indexOf(variables.getVariables().get(j)+"_"+number.get(j))+1)+" ";
                }
            }
            for (int b = 0; b < algebra.getOperations().size(); b++) {
                if (Objects.equals(algebra.getOperations().get(b).getOpName(), variables.getOpName())) {
                    s+=(cnfFileHelper.variableCode.indexOf(variables.getResult()+"_"+algebra.getOperations().get(b).getOpTable().get(decimalNumber))+1)+" 0";
                    cnfFileHelper.line.add(s);
                    break;
                }
            }
            return;
        }
        for (int i = 0; i < base; i++) {
            for (int k=0;k<number.size();k++)
            {
                if(number.get(k)==-1)
                {
                    number.set(k, i);
                    printAllMatches(base, number, variables, algebra,cnfFileHelper); // wywołaj funkcję rekurencyjnie dla nowej liczby
                    number.set(k, -1);
                    break;
                }
            }
        }
    }
    public static void equationToCNF(List<EquationTable> eq,Algebra algebra,CnfFileHelper cnfFileHelper)
    {
        getOneFromAll(algebra,eq.get(0).getResult(),cnfFileHelper);
        for(int i=eq.size()-1;i>=0;i--) {
            String s="";
            boolean hasVariable= false;
            boolean flag;
            EquationTable row=eq.get(i);
            List<Integer> intEQ=new ArrayList<>();
            for(int j=0;j<row.getVariables().size();j++) {

                s=row.getVariables().get(j);
                char c = s.charAt(0);
                if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
                {
                    hasVariable=true;
                    intEQ.add(-1);//zaznaczam zmienną jako -1
                    if((cnfFileHelper.usedVariables.isEmpty()&&c=='w'&&s.length()==1)){ //warunek, gdy pierwsza napotkana zmienna to w
                        cnfFileHelper.usedVariables.add(c);
                        getOneFromAll(algebra,s,cnfFileHelper);
                    }
                    else if((cnfFileHelper.usedVariables.isEmpty()&&c!='w'&&s.length()==1)) { //warunek, gdy nadal nie mamy zmiennych
                        cnfFileHelper.usedVariables.add(c);
                        getOneFromAll(algebra,s,cnfFileHelper);
                    }
                    else if(s.length()<2) { //mamy zmienne, ale nie wynikowe
                        flag=false;
                        for (Character usedVariable : cnfFileHelper.usedVariables) {
                            if (c == usedVariable) {
                                flag = true; //sprawdzamy, czy zmienna nie jest duplikatem
                                break;
                            }
                        }
                        if(!flag) {//nie jest więc dołączany ją do formuły
                            cnfFileHelper.usedVariables.add(c);
                            getOneFromAll(algebra,s,cnfFileHelper);
                        }
                    }
                }
                else
                    intEQ.add(Integer.parseInt(s));//przepisuje stałą
            }
            if(!hasVariable) //funkcja ma same stałe wiec po prostu wybieram odpowiednią zmienną
            {
                int h=decodeNumber(intEQ, algebra.getCardinality());
                for(int b=0;b<algebra.getOperations().size();b++)
                {
                    if(Objects.equals(algebra.getOperations().get(b).getOpName(), row.getOpName()))
                    {
                        if(!cnfFileHelper.variableCode.contains(row.getResult() + "_0"))
                        {
                            for(int z=0;z< algebra.getCardinality();z++) //długi nawias np (x1 ||x2 ||x3) zapewniający conajmniej 1 jedynkę
                                cnfFileHelper.variableCode.add(row.getResult()+"_"+z);
                        }
                        cnfFileHelper.line.add((cnfFileHelper.variableCode.indexOf(row.getResult() + '_' + algebra.getOperations().get(b).getOpTable().get(h))+1) +" 0");
                        break;
                    }
                }
            }
            else if(i!=0)
                getOneFromAll(algebra,row.getResult(),cnfFileHelper);
            if(hasVariable)
                printAllMatches(algebra.getCardinality(),intEQ,row,algebra,cnfFileHelper);
        }
    }
    public static void doCNF (List<EquationTable> left,List<EquationTable> right, Algebra algebra,CnfFileHelper cnfFileHelper)
    {
        equationToCNF(left,algebra,cnfFileHelper);
        equationToCNF(right,algebra,cnfFileHelper);
        int leftRIndex=cnfFileHelper.variableCode.indexOf(left.get(0).getResult()+"_0");
        int rightRIndex=cnfFileHelper.variableCode.indexOf(right.get(0).getResult()+"_0");
        for(int i=0;i< algebra.getCardinality();i++)
        {
            cnfFileHelper.line.add("-"+ (leftRIndex + i+1)+" "+(rightRIndex + i+1)+" 0");
            cnfFileHelper.line.add((leftRIndex + i+1)+" -"+(rightRIndex + i+1)+" 0");
        }
    }
}

