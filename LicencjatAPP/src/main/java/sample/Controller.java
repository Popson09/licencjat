package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.tools.ModelIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sat4j.specs.*;
import org.sat4j.reader.*;

public class Controller {
    private final Algebra algebra = new Algebra();
    private final List<EquationTable> equationTables=new ArrayList<>();
    private final List<CheckEquationCorrectnessReturn> left=new ArrayList<>();
    private final List<CheckEquationCorrectnessReturn> right=new ArrayList<>();
    private final CnfFileHelper cnfFileHelper= new CnfFileHelper();
    private  ShowFileController showFileController;
    boolean flag=false;
    boolean statusFlag=false;
    List<TextField> textFields=new ArrayList<>();
    List<HBox> hBoxes= new ArrayList<>();
    @FXML
    VBox vbox;
    @FXML
    TextField eqCount;
    @FXML
    HBox hbox;
    @FXML
    TextField eq1;
    @FXML
    Button addButton;
    @FXML
    Text checkResultText;
    public void addEq() {
        HBox hBox= new HBox();
        hBox.setPrefSize(600,50);
        TextField textField=new TextField();
        textField.setPrefSize(375,50);
        textFields.add(textField);
        Button removeButton= new Button();
        removeButton.setText("Usuń równanie");
        removeButton.setPrefSize(100,50);
        removeButton.setOnAction(actionEvent -> {
            textFields.remove(textField);
            vbox.getChildren().remove(hBox);
            hBoxes.remove(hBox);
            if (hBoxes.size()==0)
                hbox.getChildren().add(addButton);
            else if (hBoxes.get(hBoxes.size()-1).getChildren().size()==2)
                hBoxes.get(hBoxes.size()-1).getChildren().add(addButton);});
        hBox.getChildren().addAll(textField,removeButton,addButton);
        hBoxes.add(hBox);
        vbox.getChildren().add(hBox);
    }
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
            System.out.println("NIe znaleziono pliku");
        }
    }
    boolean checkEQ(List<CheckEquationCorrectnessReturn> left,List<CheckEquationCorrectnessReturn> right,StringBuilder leftMessage,StringBuilder rightMessage,String eq,int i)
    {
        boolean fl=true;
        left.add(ParseFunctions.checkEquationCorrectness(algebra, eq.split("=")[0]));
        right.add(ParseFunctions.checkEquationCorrectness(algebra, eq.split("=")[1]));
        if(!left.get(i).isCorrect||!right.get(i).isCorrect)
            fl=false;
        leftMessage.append("Równanie ").append(i+1).append(": ").append(left.get(i).message).append("\n");
        rightMessage.append("Równanie ").append(i+1).append(": ").append(right.get(i).message).append("\n");
        return fl;
    }
    boolean checkEqEmpty(String eq)
    {
        if (eq.equals(""))
        {
            checkResultText.setFill(Color.valueOf("#FF0000"));
            checkResultText.setText("Zostawiłeś puste pole dla równania!");
            return false;
        } else  if(eq.split("=").length!=2)
        {
            checkResultText.setFill(Color.valueOf("#FF0000"));
            checkResultText.setText("Równanie posiada nieprawidłową liczbę znaków równości !");
           return false;

        }
        else
            return true;
    }
    @FXML
    void checkText() {
        if (!algebra.isLoad) {
            checkResultText.setFill(Color.valueOf("#FF0000"));
            checkResultText.setText("Nie wczytałeś ciała algebry!");
            return;
        }
        statusFlag=true;
        StringBuilder leftMessage= new StringBuilder();
        StringBuilder rightMessage= new StringBuilder();
        left.clear();
        right.clear();
        String eq1Text = eq1.getText();

        statusFlag=checkEqEmpty(eq1Text);
        if (!statusFlag)
            return;
        statusFlag=checkEQ(left,right,leftMessage,rightMessage,eq1Text,0);

        for (int i=0;i<textFields.size();i++) {
            statusFlag=checkEqEmpty(textFields.get(i).getText());
            if (!statusFlag)
                return;
            statusFlag = checkEQ(left, right, leftMessage, rightMessage, textFields.get(i).getText(),i+1);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("showFile.fxml"));
            Parent root = loader.load();
            // Uzyskaj kontroler dla nowego okna i przekaż mu jedną klasę
            showFileController = loader.getController();
            showFileController.showEqStatus(leftMessage.toString(),rightMessage.toString());
            // Utwórz nowe okno
            Stage noweOkno = new Stage();
            noweOkno.setTitle("Poprawność równań");
            noweOkno.setScene(new Scene(root));
            noweOkno.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if(statusFlag) {
            checkResultText.setFill(Color.valueOf("#00FF00"));
            checkResultText.setText("Równania Poprawne");
        }
        else {
            checkResultText.setFill(Color.valueOf("#FF0000"));
            checkResultText.setText("Równania Niepoprawne");
        }

    }
    public void showReadAlgebra()  {
        if (!algebra.isLoad) {
            checkResultText.setFill(Color.valueOf("#FF0000"));
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
            noweOkno.setTitle("Wczytana Algebra");
            noweOkno.setScene(new Scene(root));
            noweOkno.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CNFreduction() {
        int w=left.size()-1;

        if (!statusFlag) {
            checkResultText.setFill(Color.valueOf("#FF0000"));
            checkResultText.setText("Podaj poprawne równania zanim przejdziesz do redukcji!");
        }
        else
        {
            cnfFileHelper.clear();
            equationTables.clear();
            for(int i=0;i< left.size();i++)
            {
                equationTables.addAll(ParseFunctions.getEquationTable(left.get(i).equations,algebra,w,i));
                System.out.println(equationTables.size());
                w=left.size()-1+equationTables.size()-1-2*i;
                equationTables.addAll(ParseFunctions.getEquationTable(right.get(i).equations,algebra,w,i));
                System.out.println(equationTables.size());
                w=left.size()-1+equationTables.size()-2*(i+1);
            }
            ParseFunctions.doCNF(equationTables,algebra,cnfFileHelper);


            String nazwaPliku = "reduction.cnf";
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
                checkResultText.setFill(Color.valueOf("#00FF00"));
                checkResultText.setText("Redukcja wykonana poprawnie " );
                flag=true;
            } catch (IOException e) {
                flag=false;
                checkResultText.setFill(Color.valueOf("#FF0000"));
                checkResultText.setText("Wystąpił błąd podczas zapisu do pliku " + nazwaPliku);
                e.printStackTrace();
            }
            StringBuilder s=new StringBuilder();
            s.append("Układ równań:\n");
            for (EquationTable equationTable : equationTables)
                s.append("(").append(equationTable.getOpName()).append(' ').append(equationTable.getVariables()).append(' ').append(equationTable.getResult()).append(")").append("\n");
            s.append("\n");
            try{
                ISolver solver = SolverFactory.newDefault();
                DimacsReader reader = new DimacsReader(solver);
                reader.parseInstance("reduction.cnf");
                ModelIterator modelIterator= new ModelIterator(solver);
                boolean satisfiable = solver.isSatisfiable();
                int count=0,x=Integer.parseInt(eqCount.getText());
                eqCount.setText("1");
                // Sprawdź, czy problem jest spełnialny
                while (modelIterator.isSatisfiable()&&count<x)
                {
                    StringBuilder var=new StringBuilder();
                    StringBuilder res= new StringBuilder();
                     VecInt arr= new VecInt(new int[]{});

                    for (int i = 1; i <= solver.nVars(); i++)
                    {
                        boolean b=modelIterator.model(i);
                        arr.push(b? -i: i);
                        if(b)
                        {

                            String []s1 =cnfFileHelper.variableCode.get(i-1).split("_");
                            if(s1[0].charAt(0)=='W')
                                res.append(s1[0]).append("=").append(s1[1]).append("| ");
                            else
                                var.append(s1[0]).append("=").append(s1[1]).append("| ");
                        }

                    }
                    solver.addClause(arr);
                    s.append("Rozwiązanie ").append(count+1).append('\n').append(var).append('\n').append(res).append("\n-------------------------\n");;
                    count++;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("showFile.fxml"));
                    Parent root = loader.load();
                    // Uzyskaj kontroler dla nowego okna i przekaż mu jedną klasę
                    showFileController = loader.getController();
                    showFileController.showRes(s.toString());
                    // Utwórz nowe okno
                    Stage noweOkno = new Stage();
                    noweOkno.setTitle("Wynik działania aplikacji");
                    noweOkno.setScene(new Scene(root));
                    noweOkno.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(count==0)
                {
                    checkResultText.setFill(Color.valueOf("#FF0000"));
                    checkResultText.setText("Nie znaleziono spełnialnego przypisania zmiennych.");
                }
                else
                {
                    checkResultText.setFill(Color.valueOf("#00FF00"));
                    checkResultText.setText("Znaleziono spełnialne przypisanie zmiennych");
                }

            } catch (ContradictionException | TimeoutException | ParseFormatException | IOException e) {
                throw new RuntimeException(e);}
        }
    }





}
