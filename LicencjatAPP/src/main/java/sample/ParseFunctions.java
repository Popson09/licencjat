package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ParseFunctions {


    public static int codeNumber(String value, int cardinality) {
        int res = 0;
        for (int i = value.length() - 1, j = 0; i >= 0; i -= 2, j++) {
            int a = value.charAt(i) - '0';
            res += a * Math.pow(cardinality, j);
        }
        return res;
    }

    public static String decodeNumber(int value, int cardinality) {
        StringBuilder res = new StringBuilder(); //index w opTable to liczba zapisana w systemie liczbowym o podłodze  cardinality;
        while (value > 1) {
            int a = value % cardinality;
            value /= cardinality;
            res.append(a);
            if (value > 1)
                res.append(',');
        }
        return res.reverse().toString();
    }

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
                    if (((c >= 'a' && c <= 'z') || (c > 'A' && c <= 'Z')) && s.length() == 1) {
                        symbol = EquationSymbols.variable;//jednoliterowy string który nie jest operatorem jest zmienną
                        break;
                    } else if ((c >= 'a' && c <= 'z') || (c > 'A' && c <= 'Z')) {
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
    public static List<EquationTable> getEquationTable(List<String> eq,Algebra algebra,int w)
    {
        List<EquationTable> res=new ArrayList<>();
        int index=-1;
        boolean opFind;
        String s;
        for(int i=0;i<eq.size();i++)
        {
            opFind=false;
            s=eq.get(i);
            for (int j = 0; j < algebra.getOperations().size(); j++) {
                if (s.equals(algebra.getOperations().get(j).getOpName())) {
                    if(index==-1)
                    {

                        index++;
                        res.add(new EquationTable(w));
                        w++;
                        res.get(index).setOpName(s);
                        res.get(index).setArity(algebra.getOperations().get(j).getArity());
                    }
                    else {
                        int a=index;
                        while(res.get(a).getVariables().size()==res.get(a).getArity())
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
            if(!opFind)
            {
                while(res.get(index).getVariables().size()==res.get(index).getArity())
                    index--;
                res.get(index).getVariables().add(s);
            }

        }
        return res;
    }
}
