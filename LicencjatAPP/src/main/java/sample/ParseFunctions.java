package sample;

import java.util.*;

public class ParseFunctions {




    public static CheckEquationCorrectnessReturn checkEquationCorrectness(Algebra algebra, String equation) {
        EquationSymbols symbol;
        Queue<String> queue = new PriorityQueue<String>();
        //String[] equationTable = equation.split(" ");//dzielimy równanie po spacji do tablicy
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
                        queue.remove(); //ściągamy symbole, nie potrzebujemy ich znać bo tulko sprawdzamy składnie
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
        List<EquationTable> res=new ArrayList<>(); // konkretna operacja np XOR(1,w1)
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
    public static String getOnefromAll(Algebra algebra,String s) //funkcja zapewniająca dokładnie 1 wystąpienie zmiennje
    {
        String CNF_form="";
        for(int z=0;z< algebra.getCardinality();z++) //długi nawias np (x1 ||x2 ||x3) zapeniający conajmniej 1
        {
            if(z==0)
                CNF_form+="(";
            CNF_form=CNF_form+s+z;
            if(z<algebra.getCardinality()-1)
                CNF_form+=" ∨ ";
            else
                CNF_form+=")^";
        }
        for(int z=0;z< algebra.getCardinality();z++){ //nawiasy (!x1 || !x2)&&(!x1 || !x3)&&(!x2 || !x3) zapewniajace conajwyżej 1
            for(int q=z+1;q< algebra.getCardinality();q++) {
                    CNF_form=CNF_form+"(¬"+s+z+" ∨ "+'¬'+s+q+")^";
            }
        }
        return CNF_form+'\n';
    }
    public static int decodeNumber(List<Integer> values, int cardinality) {
        int res = 0;
        for (int i = values.size() - 1, j = 0; i >= 0; i--, j++) {

            res += values.get(i) * Math.pow(cardinality, j);
        }
        return res;
    }

    public static void printAllMatches(int base, List<Integer> number, EquationTable variables, Algebra algebra, StringBuilder sb) {
        //System.out.println(number);
        if (!number.contains(-1)) { // jeśli nie ma już niewiadomych cyfr

            int decimalNumber = decodeNumber(number, algebra.getCardinality()); // oblicz wartość liczby w systemie dziesiętnym
            char c = ' ';
            sb.append("(");
            for (int j = 0; j < number.size(); j++) {
                c = variables.getVariables().get(j).charAt(0);
                if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                    sb.append("¬").append(variables.getVariables().get(j)).append(number.get(j)).append(" ∨ ");
                }
            }

            for (int b = 0; b < algebra.getOperations().size(); b++) {
                if (Objects.equals(algebra.getOperations().get(b).getOpName(), variables.getOpName())) {
                    sb.append(variables.getResult()).append(algebra.getOperations().get(b).getOpTable().get(decimalNumber)).append(")^");
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
                    printAllMatches(base, number, variables, algebra, sb); // wywołaj funkcję rekurencyjnie dla nowej liczby
                    number.set(k, -1);
                    break;
                }
            }
        }
    }
    public static String equationToCNF(List<EquationTable> eq,Algebra algebra,List<Character> usedVariables)
    {
        String CNF_form="";
        CNF_form=CNF_form+getOnefromAll(algebra,eq.get(0).getResult());

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
                    if((usedVariables.isEmpty()&&c=='w'&&s.length()==1)){ //warunek gdy pierwsza napotkana zmienna to w
                        usedVariables.add(c);
                        CNF_form=CNF_form+getOnefromAll(algebra,s);
                    }
                    else if((usedVariables.isEmpty()&&c!='w'&&s.length()==1)) { //warunek gdy nadal nie mamu zmiennych
                        usedVariables.add(c);
                        CNF_form=CNF_form+getOnefromAll(algebra,s);
                    }
                    else if(s.length()<2) { //mamy zmienne ale nie wynikowe
                        flag=false;
                        for (Character usedVariable : usedVariables) {
                            if (c == usedVariable) {
                                flag = true; //sprawdzamy czy zmienna nie jest duplikatem
                                break;
                            }
                        }
                        if(!flag) {//nie jest więc dołączany ją do formuły
                            usedVariables.add(c);
                            CNF_form=CNF_form+getOnefromAll(algebra,s);
                        }
                    }
                }
                else
                    intEQ.add(Integer.parseInt(s));//przepisuje stałą
            }
            if(!hasVariable) //funkcja ma  same stałe wiec po prostu wybieram odpowiednią zmienną
            {
                int h=decodeNumber(intEQ, algebra.getCardinality());
                for(int b=0;b<algebra.getOperations().size();b++)
                {
                    if(Objects.equals(algebra.getOperations().get(b).getOpName(), row.getOpName()))
                    {
                        CNF_form=CNF_form+row.getResult()+algebra.getOperations().get(b).getOpTable().get(h)+" ^ ";
                        break;
                    }
                }
            }
            else if(i!=0)
                CNF_form=CNF_form+getOnefromAll(algebra,row.getResult());
            if(hasVariable)
            {

                StringBuilder sb= new StringBuilder();

                printAllMatches(algebra.getCardinality(),intEQ,row,algebra,sb);
                CNF_form+= sb.toString();
            }
            CNF_form+='\n';

        }
        return CNF_form;
    }

    public static String doCNF (List<EquationTable> left,List<EquationTable> right, Algebra algebra)
    {
        List<Character> usedVariables=new ArrayList<>(); //tablica wyłapująca powtórzenia zmiennych
        String CNF_form="";
        CNF_form+=equationToCNF(left,algebra,usedVariables);
        CNF_form+=equationToCNF(right,algebra,usedVariables);
        String leftR=left.get(0).getResult();
        String rightR=right.get(0).getResult();
        for(int i=0;i< algebra.getCardinality();i++)
        {
            CNF_form=CNF_form+"(¬"+leftR+i+" ∨ "+rightR+i+")^("+leftR+i+" ∨ ¬"+rightR+i+")";
            if(i<algebra.getCardinality()-1)
                CNF_form+="^";
        }


        System.out.println(CNF_form);
        return "";
    }

}

