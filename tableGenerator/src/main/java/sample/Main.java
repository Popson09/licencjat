package sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static String systemChange(int liczba, int system, int len) {
        StringBuilder sb = new StringBuilder();
        if(liczba==0)
        {
            for (int i=0;i < len-1;i++) {
                sb.insert(0, "0,");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }


        while (liczba > 0) {
            int reszta = liczba % system;
            sb.insert(0, reszta + ",");
            liczba /= system;
        }

        // Usuwanie przecinka na końcu
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        while (sb.length() < 2*len-3) {
            sb.insert(0, "0,");
        }

        return sb.toString();
    }
    public static void main(String[] args) {

        Scanner scanner= new Scanner(System.in);
        Random random=new Random();


        System.out.println("Podaj Nazwę pliku ");

        String nazwaPliku = scanner.nextLine();
        File plik = new File(nazwaPliku);
        try {
            if(plik.createNewFile()) {
                System.out.println("Utworzono plik " + nazwaPliku);
            } else {
                System.out.println("Plik " + nazwaPliku + " już istnieje");
            }

            FileWriter fileWriter = new FileWriter(plik);

            fileWriter.write("<?xml version=\"1.0\"?>\n");
            fileWriter.write("<algebra>\n");
            fileWriter.write(" <basicAlgebra>\n");
            System.out.println("Podaj Nazwę algebry ");
            String name= scanner.nextLine();
            fileWriter.write("  <algName>"+name+"</algName>\n");
            System.out.println("Podaj Liczność ");
            int c= scanner.nextInt();
            fileWriter.write("  <cardinality>"+ c +"</cardinality>\n");
            fileWriter.write("  <operations>\n");
            System.out.println("Podaj Liczbę operacji ");
            int opNum= scanner.nextInt();
            for(int x=0;x<opNum;x++)
            {
                Scanner scanner1= new Scanner(System.in);
                Scanner scanner2= new Scanner(System.in);
                fileWriter.write("   <op>\n");
                fileWriter.write("    <opSymbol>\n");
                System.out.println("Podaj Nazwę operacji ");
                String n2= scanner1.nextLine();
                fileWriter.write("     <opName>"+n2+"</opName>\n");
                System.out.println("Podaj arność operacji ");
                int a= scanner2.nextInt();
                fileWriter.write("     <arity>"+ a +"</arity>\n");
                fileWriter.write("    </opSymbol>\n");
                int w= (int)Math.pow(c,a-1);
                fileWriter.write("    <opTable>\n");
                fileWriter.write("    <intArray>\n");
                for(int i=0;i<w;i++)
                {
                    String s=(Main.systemChange(i,c,a));
                    fileWriter.write("     <row r=\"["+s+"]\">");
                    for (int j=0;j<c;j++)
                    {
                        int k=random.nextInt(c);
                        fileWriter.write(String.valueOf(k));
                        if(j<c-1)
                            fileWriter.write(",");
                    }
                    fileWriter.write("</row>\n");
                }
                fileWriter.write("   </intArray>\n");


                fileWriter.write("    </opTable>\n");
                fileWriter.write("   </op>\n");

            }
            fileWriter.write("  </operations>\n");
            fileWriter.write(" </basicAlgebra>\n");
            fileWriter.write("</algebra>\n");

            fileWriter.close();
            System.out.println("Generowanie algebry zakończone pomyślnie");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}