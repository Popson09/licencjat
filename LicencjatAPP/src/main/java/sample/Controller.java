package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.sat4j.minisat.SolverFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.sat4j.core.*;
import org.sat4j.specs.*;
import org.sat4j.reader.*;

public class Controller {
    private final Algebra algebra = new Algebra();
    private List<EquationTable> equationLeft=new ArrayList<>();
    private List<EquationTable> equationRight=new ArrayList<>();
    private CheckEquationCorrectnessReturn left=new CheckEquationCorrectnessReturn();
    private CheckEquationCorrectnessReturn right=new CheckEquationCorrectnessReturn();
    private final CnfFileHelper cnfFileHelper= new CnfFileHelper();
    boolean flag=false;
    @FXML
    TextField leftSiteText;
    @FXML
    TextField rightSiteText;
    @FXML
    Text checkResultText;
    public void parseFile() {
        algebra.clear();
        algebra.isLoad = true;
        try {
            // utworzenie okna dialogowego wyboru pliku
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Wybierz plik zawierający algebrę");
            File file = fileChooser.showOpenDialog(null);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file); //parsowanie pliku za pomocą wbudowanej biblioteki
            doc.getDocumentElement().normalize();
            algebra.setAlgName(doc.getDocumentElement().getElementsByTagName("algName").item(0).getTextContent());
            algebra.setCardinality(doc.getDocumentElement().getElementsByTagName("cardinality").item(0).getTextContent());
            NodeList opList = doc.getElementsByTagName("op"); //wyciągnięcie listy operacji
            for (int i = 0; i < opList.getLength(); i++) {
                Operation op = new Operation();
                Node node = opList.item(i);
                Element element = (Element) node;
                op.setArity(element.getElementsByTagName("arity").item(0).getTextContent());
                op.setOpName(element.getElementsByTagName("opName").item(0).getTextContent());
                NodeList intArray = element.getElementsByTagName("row"); //wyciągnięcie tabeli operacji
                int arrayIndex = 0;
                for (int j = 0; j < intArray.getLength(); j++) {
                    Node intArrayNode = intArray.item(j);
                    Element intArrayElement = (Element) intArrayNode; //wielkość tablicy to pow(cardinality,arity)
                    //String code=intArrayElement.getAttribute("r"); //kod w opTable funkcje dekodujące w klacie ParseFunction
                    String intRow = intArrayElement.getTextContent();
                    StringBuilder intNumber= new StringBuilder();
                    for (int k = 0; k < intRow.length(); k++) { //wiersz ma długość cardinality np 00,01,02,03
                        if(intRow.charAt(k)!=',' )
                        {
                                intNumber.append(intRow.charAt(k));
                        }
                        if(intRow.charAt(k)==',' || k ==intRow.length()-1)
                        {
                            op.setOpTableValue(arrayIndex, Integer.parseInt(intNumber.toString()));
                            arrayIndex++;
                            intNumber = new StringBuilder();
                        }
                    }
                }
                algebra.setOperationsValue(i, op);//wrzucenie operacji do listy w algebrze
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void checkText() {
        if (!algebra.isLoad) {
            checkResultText.setText("Nie wczytałeś ciała algebry!");
            return;
        }
        String ls = leftSiteText.getText();
        String rs = rightSiteText.getText();
        if (ls.length()==0 || rs.length()==0)
            checkResultText.setText("Nie podałeś równania!");
        else
        {
            left = ParseFunctions.checkEquationCorrectness(algebra, ls);
            right = ParseFunctions.checkEquationCorrectness(algebra, rs);
            checkResultText.setText("Lewe równanie: " + left.message + "\nPrawe równanie: " + right.message);
        }
    }
    public void showReadAlgebra() {
        algebra.showAlgebraFile();
    }
    public void CNFreduction() {
        if (!left.isCorrect || !right.isCorrect)
            checkResultText.setText("Podaj poprawne równania zanim przejdzesz do redukcji!");
        else
        {
            cnfFileHelper.clear();
            equationLeft=ParseFunctions.getEquationTable(left.equations,algebra,0);
            equationRight=ParseFunctions.getEquationTable(right.equations,algebra,equationLeft.size());
            for (EquationTable table : equationLeft)
                System.out.println(table.getOpName() + ' ' + table.getVariables() + ' ' + table.getResult());
            System.out.println("-------------------------");
            for (EquationTable equationTable : equationRight)
                System.out.println(equationTable.getOpName() + ' ' + equationTable.getVariables() + ' ' + equationTable.getResult());
            System.out.println("-------------------------");
            ParseFunctions.doCNF(equationLeft,equationRight,algebra,cnfFileHelper);
            flag=true;
            //System.out.println(cnfFileHelper.line);
            //System.out.println(cnfFileHelper.variableCode);

            String nazwaPliku = "reduction.txt";
            File plik = new File(nazwaPliku);
            try {
                if(plik.createNewFile()) {
                    System.out.println("Utworzono plik " + nazwaPliku);
                } else {
                    System.out.println("Plik " + nazwaPliku + " już istnieje");
                }

                FileWriter fileWriter = new FileWriter(plik);
                fileWriter.write("c ");
                for(int k=0;k<cnfFileHelper.variableCode.size();k++) {
                    fileWriter.write((k+1)+"="+cnfFileHelper.variableCode.get(k) + " ");
                }
                fileWriter.write(System.lineSeparator()+cnfFileHelper.name+" "+cnfFileHelper.variableCode.size()+" "+cnfFileHelper.line.size()+'\n');
                for(String element : cnfFileHelper.line) {
                    fileWriter.write(element + System.lineSeparator());
                }
                fileWriter.close();
                checkResultText.setText("Redukcja wykonana poprawnie " );
            } catch (IOException e) {
                checkResultText.setText("Wystąpił błąd podczas zapisu do pliku " + nazwaPliku);
                e.printStackTrace();
            }
        }


    }

    public void runSatSolver()  {
        if(flag)
        {
           // System.out.println(cnfFileHelper.usedVariables);
            //System.out.println(cnfFileHelper.line.size());
            try{
                ISolver solver = SolverFactory.newDefault();
                DimacsReader reader = new DimacsReader(solver);
                reader.parseInstance("reduction.txt");
                boolean satisfiable = solver.isSatisfiable();
                // Sprawdź, czy problem jest spełnialny
                if (satisfiable)
                {
                    checkResultText.setText("Znaleziono spełnialne przypisanie zmiennych, znajdują się w pliku results.txt");
                    String nazwaPliku = "result.txt";
                    File plik = new File(nazwaPliku);
                    try {
                        if(plik.createNewFile()) {
                            System.out.println("Utworzono plik " + nazwaPliku);
                        } else {
                            System.out.println("Plik " + nazwaPliku + " już istnieje");
                        }
                        FileWriter fileWriter = new FileWriter(plik);
                        for (int i = 1; i <= solver.nVars(); i++)
                            fileWriter.write(cnfFileHelper.variableCode.get(i-1) +" = " + solver.model(i)+ System.lineSeparator());
                        fileWriter.close();
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                    checkResultText.setText("Nie znaleziono spełnialnego przypisania zmiennych.");
            } catch (ContradictionException | TimeoutException | ParseFormatException | IOException e) {
                throw new RuntimeException(e);}
            flag=false;
        }
        else
            checkResultText.setText("Nie wykonałeś redukcji!");
    }
}
