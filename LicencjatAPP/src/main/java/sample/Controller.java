package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.sat4j.minisat.SolverFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.sat4j.specs.*;
import org.sat4j.reader.*;

public class Controller {
    private final Algebra algebra = new Algebra();
    private final List<List<EquationTable>> equationLeft=new ArrayList<>();
    private final List<List<EquationTable>> equationRight=new ArrayList<>();
    private final List<CheckEquationCorrectnessReturn> left=new ArrayList<>();
    private final List<CheckEquationCorrectnessReturn> right=new ArrayList<>();
    private final CnfFileHelper cnfFileHelper= new CnfFileHelper();
    private  ShowFileController showFileController;
    boolean flag=false;
    boolean statusFlag=false;
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
                    Element intArrayElement = (Element) intArrayNode; //Wielkość tablicy to pow. (cardinality,arity)
                    //String code=intArrayElement.getAttribute("r"); //kod w opTable funkcje dekodujące w klasie ParseFunction
                    String intRow = intArrayElement.getTextContent();
                    StringBuilder intNumber= new StringBuilder();
                    for (int k = 0; k < intRow.length(); k++) { //Wiersz ma długość cardinality np. 00,01,02,03
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
        statusFlag=true;
        String ls=leftSiteText.getText();
        String rs = rightSiteText.getText();
        String [] lsTable=ls.split(";");
        String [] rsTable=rs.split(";");
        if (ls.equals("") || rs.equals(""))
            checkResultText.setText("Nie podałeś równania!");
        else if (lsTable.length!= rsTable.length)
            checkResultText.setText("Podałeś niezgodną liczbę równań!");
        else
        {
            StringBuilder leftMessage= new StringBuilder();
            StringBuilder rightMessage= new StringBuilder();

            for(int i=0;i< lsTable.length;i++)
            {
                left.add(i,ParseFunctions.checkEquationCorrectness(algebra, lsTable[i]));
                right.add(i,ParseFunctions.checkEquationCorrectness(algebra, rsTable[i]));
                if(!left.get(i).isCorrect||!right.get(i).isCorrect)
                    statusFlag=false;
                leftMessage.append("Równanie ").append(i).append(": ").append(left.get(i).message).append("\n");
                rightMessage.append("Równanie ").append(i).append(": ").append(right.get(i).message).append("\n");
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("showFile.fxml"));
                Parent root = loader.load();

                // Uzyskaj kontroler dla nowego okna i przekaż mu jedną klasę
                showFileController = loader.getController();
                showFileController.showEqStatus(leftMessage.toString(),rightMessage.toString());

                // Utwórz nowe okno
                Stage noweOkno = new Stage();
                noweOkno.setScene(new Scene(root));
                noweOkno.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(statusFlag)
                checkResultText.setText("Równania Poprawne");
            else
                checkResultText.setText("Równania Niepoprawne");
        }
    }
    public void showReadAlgebra()  {
        if (!algebra.isLoad) {
            checkResultText.setText("Nie wczytałeś ciała algebry!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("showFile.fxml"));
            Parent root = loader.load();

            // Uzyskaj kontroler dla nowego okna i przekaż mu jedną klasę
            showFileController = loader.getController();
            showFileController.setText(algebra);

            // Utwórz nowe okno
            Stage noweOkno = new Stage();
            noweOkno.setScene(new Scene(root));
            noweOkno.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CNFreduction() {
        int w=0;

        if (!statusFlag)
            checkResultText.setText("Podaj poprawne równania zanim przejdziesz do redukcji!");
        else
        {
            cnfFileHelper.clear();
            for(int i=0;i< left.size();i++)
            {
                equationLeft.add(new ArrayList<>());
                equationRight.add(new ArrayList<>());
                equationLeft.set(i,ParseFunctions.getEquationTable(left.get(i).equations,algebra,w));
                w+=equationLeft.get(i).size();
                equationRight.set(i,ParseFunctions.getEquationTable(right.get(i).equations,algebra,w));
                w+=equationRight.get(i).size();
                ParseFunctions.doCNF(equationLeft.get(i),equationRight.get(i),algebra,cnfFileHelper);
            }

            flag=true;
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

        StringBuilder s=new StringBuilder();
        for(int i=0;i<equationLeft.size();i++)
        {
            s.append("-------------------------\n");
            eqOutput(s, i, equationLeft);
            eqOutput(s, i, equationRight);
            s.append(equationLeft.get(i).get(0).getResult()).append(" = ").append(equationRight.get(i).get(0).getResult()).append('\n');
        }
        s.append("-------------------------\n\nWynik:\n\n");
        if(flag)
        {
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
                        {
                            fileWriter.write(cnfFileHelper.variableCode.get(i-1) +" = " + solver.model(i)+ System.lineSeparator());
                            if(solver.model(i))
                            {
                                String []s1 =cnfFileHelper.variableCode.get(i-1).split("_");
                                s.append(s1[0]).append("=").append(s1[1]).append('\n');

                            }
                        }

                        fileWriter.close();
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("showFile.fxml"));
                        Parent root = loader.load();
                        // Uzyskaj kontroler dla nowego okna i przekaż mu jedną klasę
                        showFileController = loader.getController();
                        showFileController.showRes(s.toString());
                        // Utwórz nowe okno
                        Stage noweOkno = new Stage();
                        noweOkno.setScene(new Scene(root));
                        noweOkno.show();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void eqOutput(StringBuilder s, int i, List<List<EquationTable>> equationLeft) {
        for (int j = 0; j< equationLeft.get(i).size(); j++)
            s.append(equationLeft.get(i).get(j).getOpName()).append(' ').append(equationLeft.get(i).get(j).getVariables()).append(' ').append(equationLeft.get(i).get(j).getResult()).append('\n');
        s.append("-------------------------\n");
    }
}
