package sample;

import java.util.*;

public class ParseFunctions {

    //pierwszy etap: Sprawdzenie, czy podane równanie jest zgodne z wczytaną algebrą
    public static CheckEquationCorrectnessReturn checkEquationCorrectness(Algebra algebra, String equation) {
        EquationSymbols symbol; //pomocniczy enum
        Queue<String> queue = new PriorityQueue<>(); //kolejka nadzorująca ilość zmiennych
        StringBuilder s;
        equation=equation.trim();
        List<String> eqList =new ArrayList<>(Arrays.asList(equation.split("[(),\\s+]+")));//lista z przekształconym równaniem
        int opIndex = 0;
        //wstępnie obrobione równanie sprawdzamy, pod względem poprawności z algebrą
        for (int i = eqList.size()- 1; i >= 0; i--) {
            symbol = EquationSymbols.empty;
            s = new StringBuilder(eqList.get(i)); //pobieramy kolejny znak równania
            for (int j = 0; j < algebra.getOperations().size(); j++) {
                if (s.toString().equals(algebra.getOperations().get(j).getOpName())) {
                    symbol = EquationSymbols.operator; //symbol znaleziony w algebrze, jako nazwa operacji
                    opIndex = j; //zapamiętujemy index operacji w algebrze
                    break;
                }
            }
            if (symbol == EquationSymbols.empty) { //symbol nie jest operacją, sprawdzamy, czy jest zmienną lub stałą
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z'))
                    symbol = EquationSymbols.variable;
                else if (c>='0'&&c<='9') { //dowolny ciąg zaczynający się od litery jest zmienną
                    try //Zabezpieczenie na wypadek, gdyby ktoś wprowadził symbol np. 2x
                    {
                        int a = Integer.parseInt(s.toString());
                        if (a >= algebra.getCardinality() || a < 0) //sprawdzamy, czy liczba należy do dziedziny
                            return new CheckEquationCorrectnessReturn("Błędny wielomian: Stała poza zakresem dziedziny", eqList, false);
                        symbol = EquationSymbols.constant;
                    }
                    catch (Exception e) {
                        return new CheckEquationCorrectnessReturn("Błędny wielomian: Niedozwolona nazwa zmiennej",eqList,false);
                    }
                }
            }
            if (symbol == EquationSymbols.constant || symbol == EquationSymbols.variable)
                queue.add(s.toString()); //dodajemy liczbę/zmienną do stosu
            else { //symbol jest operatorem
                for (int j = 0; j < algebra.getOperations().get(opIndex).getArity(); j++) //znajdujemy liczbę argumentów powiązanych z operacją
                {
                    if (queue.isEmpty())
                        return new CheckEquationCorrectnessReturn("Błędny wielomian: Pusty stos",eqList,false);//mamy na mało symboli na stosie, czyli równanie złe
                    else {
                        queue.remove(); //ściągamy odpowiednią liczbę zmiennych
                    }
                }
                queue.add("w");//dodajemy zmienną wynikową do stosu
            }
        }
        if(queue.size() > 1) //na koniec w stosie powinien zostać tylko jeden symbol
            return new CheckEquationCorrectnessReturn("Błędny wielomian: Nadmiarowa liczba argumentów",eqList,false);
        return new CheckEquationCorrectnessReturn( "Wielomian poprawny",eqList,true);
    }
    //Na podstawie listy symboli równania z poprzedniej funkcji budujemy listę pojedynczych działań równania
    public static List<EquationTable> getEquationTable(List<String> eq,Algebra algebra,int w, int number)
    {
         //w to zmienna indeksująca symbol oznaczający wynik działania
        List<EquationTable> res=new ArrayList<>(); // Lista działań równania np. xor(1,x) &0
        int index=-1; //zmienna indeksująca listę
        boolean opFind;
        String s;
        if(eq.size()==1)
        {
            res.add(new EquationTable());
            res.get(0).setResult("W"+number);
            res.get(0).setOpName("WW");
            res.get(0).setArity(0);
            res.get(0).getVariables().add(eq.get(0));
            return res;
        }
        for (String value : eq) {
            opFind = false;
            s = value; //pobieramy pojedynczy symbol równania
            for (int j = 0; j < algebra.getOperations().size(); j++) {
                if (s.equals(algebra.getOperations().get(j).getOpName())) { //symbol jest operatorem

                    index++; //zwiększam indeks listy
                    res.add(new EquationTable());
                    if(index==0)
                        res.get(index).setResult("W"+number); //lewej i prawej stronie równania przypisuje ten sam numer
                    else
                        res.get(index).setResult("W"+w);
                    w++; //zwiększam indeks numeracji działania
                    res.get(index).setOpName(s); //wrzucam do tablicy nazwę operacji i ilość argumentów
                    res.get(index).setArity(algebra.getOperations().get(j).getArity());
                    if(index!=0)
                    {
                        int a = index-1;
                        while (res.get(a).getVariables().size() == res.get(a).getArity()) //jeżeli jest to zagnieżdżona funkcja szukam jej początku
                            a--;
                        res.get(a).getVariables().add(res.get(index).getResult());
                    }
                    opFind = true;
                    break;
                }
            }
            if (!opFind) //nie jest to operacja wpisuje po prostu zmienną
            {
                while (res.get(index).getVariables().size() == res.get(index).getArity()) //zapobieganie błędom przy działaniach zagnieżdżonych
                    index--;
                res.get(index).getVariables().add(s);
            }
        }
        return res;
    }
    //pierwsza funkcja generująca CNF
    public static void getOneFromAll(Algebra algebra,String s,CnfFileHelper cnfFileHelper)
    {
        StringBuilder line= new StringBuilder();
        for(int z=0;z< algebra.getCardinality()-1;z++) //generowanie formuły (x1||x2||...||xn) n={1 ... algebra.getCardinality()}
        {
            cnfFileHelper.variableCode.add(s+"_"+z);//dodaje symbol jako użytą zmienną w formule
            line.append(cnfFileHelper.variableCode.size()).append(" ");//dodaje do formuły po odpowiednim prze-indeksowaniu
        }
        cnfFileHelper.variableCode.add(s+"_"+(algebra.getCardinality()-1));
        line.append(cnfFileHelper.variableCode.size()).append(" 0");//kończę formułę
        cnfFileHelper.line.add(line.toString());
        for(int z=0;z< algebra.getCardinality();z++){ // generowanie formuły (!xn ||!xm) n={1 ... algebra.getCardinality()} m={1 ... algebra.getCardinality()}
            for(int q=z+1;q< algebra.getCardinality();q++) {
                cnfFileHelper.line.add("-"+ (cnfFileHelper.variableCode.indexOf(s + "_" + z)+1) +" -"+ (cnfFileHelper.variableCode.indexOf(s + "_" + q)+1)+" 0");
            }
        }
    }
    //dekodowanie numeru zapisu w tablicy operacji kod to system liczbowy o podstawie cardinality
    public static int decodeNumber(List<Integer> values, int cardinality) {
        int res = 0;
        for (int i = values.size() - 1, j = 0; i >= 0; i--, j++) {

            res += values.get(i) * Math.pow(cardinality, j);
        }
        return res;
    }
    //funkcja rekurencyjna wywoływana, gdy działanie ma jakieś zmienne
    public static void printAllMatches(int base, List<Integer> number, EquationTable variables, Algebra algebra,CnfFileHelper cnfFileHelper) {
        if (!number.contains(-1)) { // jeśli nie ma już niewiadomych cyfr
            int decimalNumber = decodeNumber(number, algebra.getCardinality()); // oblicz wartość liczby w systemie dziesiętnym
            char c;
            StringBuilder s= new StringBuilder();
            for (int j = 0; j < number.size(); j++) {
                c = variables.getVariables().get(j).charAt(0);
                if ((c >= 'a' && c <= 'z') ||c=='W') { //sprawdzamy, gdzie w oryginalnym działaniu była zmienna
                    s.append("-").append(cnfFileHelper.variableCode.indexOf(variables.getVariables().get(j) + "_" + number.get(j)) + 1).append(" ");
                }
            }
            for (int b = 0; b < algebra.getOperations().size(); b++) { //szukamy powiązanej operacji, by móc zdekodować wynik działania
                if (Objects.equals(algebra.getOperations().get(b).getOpName(), variables.getOpName())) {
                    s.append(cnfFileHelper.variableCode.indexOf(variables.getResult() + "_" + algebra.getOperations().get(b).getOpTable().get(decimalNumber)) + 1).append(" 0");
                    cnfFileHelper.line.add(s.toString());
                    break;
                }
            }
            return;
        }
        //rekurencyjne wywołania zmienna oznaczona jako -1
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
    //funkcja zarządzająca zamianą na CNF
    public static void doCNF (List<EquationTable> eq, Algebra algebra,CnfFileHelper cnfFileHelper)
    {
        for(int i=eq.size()-1;i>=0;i--) {
            String s;
            boolean hasVariable= false;
            EquationTable row=eq.get(i); //pobieramy działanie
            if(!cnfFileHelper.usedVariables.contains(row.getResult())){ //sprawdzenie, czy zmienna jest duplikatem
                cnfFileHelper.usedVariables.add(row.getResult());
                getOneFromAll(algebra,row.getResult(),cnfFileHelper);
            }


            if(row.getOpName().equals("WW"))
            {
                char c=row.getVariables().get(0).charAt(0);
                if ((c >= 'a' && c <= 'z') ||c=='W')
                {
                    if(!cnfFileHelper.usedVariables.contains(row.getVariables().get(0))){ //sprawdzenie, czy zmienna jest duplikatem
                        cnfFileHelper.usedVariables.add(row.getVariables().get(0));
                        getOneFromAll(algebra,row.getVariables().get(0),cnfFileHelper);
                    }
                    int leftRIndex=cnfFileHelper.variableCode.indexOf(row.getVariables().get(0)+"_0");
                    int rightRIndex=cnfFileHelper.variableCode.indexOf(row.getResult()+"_0");
                    for(int k=0;k< algebra.getCardinality();k++)
                        cnfFileHelper.line.add("-"+ (leftRIndex + k+1)+" "+(rightRIndex + k+1)+" 0");
                    continue;
                }
                cnfFileHelper.line.add((cnfFileHelper.variableCode.indexOf(row.getResult()+"_"+row.getVariables().get(0))+1) +" 0");
                continue;
            }
            List<Integer> intEQ=new ArrayList<>();
            for(int j=0;j<row.getVariables().size();j++) {

                s=row.getVariables().get(j);
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z') ||c=='W')
                {
                    hasVariable=true;
                    intEQ.add(-1);//zaznaczam zmienną jako -1 dla funkcji rekurencyjnej
                    if(!cnfFileHelper.usedVariables.contains(s)){ //sprawdzenie, czy zmienna jest duplikatem
                        cnfFileHelper.usedVariables.add(s);
                        getOneFromAll(algebra,s,cnfFileHelper);
                    }
                }
                else
                    intEQ.add(Integer.parseInt(s));//przepisuje stałą dla funkcji rekurencyjnej
            }
            if(!hasVariable) //funkcja ma same stałe wiec po prostu wybieram odpowiednią zmienną
            {
                int h=decodeNumber(intEQ, algebra.getCardinality());
                for(int b=0;b<algebra.getOperations().size();b++)
                {
                    if(Objects.equals(algebra.getOperations().get(b).getOpName(), row.getOpName()))
                    {
                        cnfFileHelper.line.add((cnfFileHelper.variableCode.indexOf(row.getResult() + '_' + algebra.getOperations().get(b).getOpTable().get(h))+1) +" 0");
                        break;
                    }
                }
            }
            else
                printAllMatches(algebra.getCardinality(),intEQ,row,algebra,cnfFileHelper);
        }
    }
}

