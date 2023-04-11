package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Controller {

    private Algebra algebra = new Algebra();
    private List<EquationTable> equationLeft=new ArrayList<>();
    private List<EquationTable> equationRight=new ArrayList<>();

    private CheckEquationCorrectnessReturn left=new CheckEquationCorrectnessReturn();
    private CheckEquationCorrectnessReturn right=new CheckEquationCorrectnessReturn();
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
                    String intNumber="";
                    for (int k = 0; k < intRow.length(); k++) { //wiersz ma długość cardinality np 00,01,02,03
                        if(intRow.charAt(k)!=',' )
                        {
                                intNumber+=intRow.charAt(k);
                        }
                        if(intRow.charAt(k)==',' || k ==intRow.length()-1)
                        {
                            op.setOpTableValue(arrayIndex, Integer.parseInt(intNumber));
                            arrayIndex++;
                            intNumber="";
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
        //System.out.println(left.equations);
        //System.out.println(right.equations);
        if (!left.isCorrect || !right.isCorrect)
            checkResultText.setText("Podaj poprawne równania zanim przejdzesz do redukcji!");
        else
        {

            equationLeft=ParseFunctions.getEquationTable(left.equations,algebra,0);
            equationRight=ParseFunctions.getEquationTable(right.equations,algebra,equationLeft.size());
            for(int i=0;i<equationLeft.size();i++)
            {
                System.out.println(equationLeft.get(i).getOpName()+' '+equationLeft.get(i).getVariables()+' '+equationLeft.get(i).getResult());
            }
            System.out.println("-------------------------");
            for (EquationTable equationTable : equationRight) {
                System.out.println(equationTable.getOpName() + ' ' + equationTable.getVariables() + ' ' + equationTable.getResult());
            }
            System.out.println("-------------------------");
            ParseFunctions.doCNF(equationLeft,equationRight,algebra);

        }
    }
}
