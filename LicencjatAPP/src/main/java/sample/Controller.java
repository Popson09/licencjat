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


public class Controller {

    private Algebra algebra = new Algebra();

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
                    for (int k = 0; k < algebra.getCardinality(); k++) { //wiersz ma długość cardinality np 00,01,02,03
                        op.setOpTableValue(arrayIndex, intRow.charAt(k * 2) - '0'); //stały format danych np 2,2,2,2
                        arrayIndex++;
                    }
                }
                algebra.setOperationsValue(i, op);//wrzucenie operacji do listy w algebrze
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(ParseFunctions.decodeNumber(35,algebra.getCardinality()));
        //System.out.println(ParseFunctions..codeNumber("2,0,3",algebra.getCardinality()));
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
            ls = ParseFunctions.checkEquationCorrectness(algebra, ls);
            rs = ParseFunctions.checkEquationCorrectness(algebra, rs);
            checkResultText.setText("Lewe równanie: " + ls + "\nPrawe równanie: " + rs);
        }


    }

    public void showReadAlgebra() {
        algebra.showAlgebraFile();
    }
}
