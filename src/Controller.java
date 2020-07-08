import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

public class Controller {

    private Grammar Grammar;

    @FXML
    public TextArea LogConsole;
    @FXML
    public TextArea GrammarArea;

    @FXML
    public Button Clean;
    @FXML
    public Button MakeFirstAndFollow;
    @FXML
    public Button MakeSyntaxMatrix;
    @FXML
    public Button LoadFile;

    @FXML
    public Button Parse;
    @FXML
    public Button LoadGrammar1;
    @FXML
    public Button LoadGrammar2;
    @FXML
    private Button LoadGrammar3;
    @FXML
    private Button LoadGrammar4;
    @FXML
    private Button LoadGrammar5;
    @FXML
    private Button LoadGrammar6;

    @FXML
    void initialize() {

        LoadGrammar1.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this,"example1.txt");
                LogConsole.appendText("Test Grammar 1\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        LoadGrammar2.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this,"example2.txt");
                LogConsole.appendText("Test Grammar 2\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        LoadGrammar3.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this, "example3.txt");
                LogConsole.appendText("Test Grammar 3\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        LoadGrammar4.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this,"example4.txt");
                LogConsole.appendText("Test Grammar 4\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        LoadGrammar5.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this,"example5.txt");
                LogConsole.appendText("Test Grammar 5\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        LoadGrammar6.setOnAction(actionEvent -> {
            try {
                Grammar = new Grammar(this,"example6.txt");
                LogConsole.appendText("Test Grammar 5\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Clean.setOnAction(actionEvent -> {
            GrammarArea.setText("");
            LogConsole.appendText("Grammar area was cleaned\n");
        });

        MakeFirstAndFollow.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            Grammar.makeFirstSet();
            Grammar.makeFollowSet();

            Grammar.printFirstSet();
            System.out.println();
            Grammar.printFollowSet();
        });


        Parse.setOnAction(actionEvent -> {
            Grammar.Parse(GrammarArea.getText());
        });

        MakeSyntaxMatrix.setOnAction(actionEvent -> {
            if(Grammar == null || Grammar.FirstSet == null){
                LogConsole.appendText("Make FIRST and FOLLOW\n");
                return;
            }

            Grammar.makeSyntaxMatrix();
            Stage newWindow = new Stage();
            GridPane root = new GridPane();
            //root.setGridLinesVisible(true);

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            for (int i = 0; i < Grammar.NonTerminals.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.NonTerminals[i])), HPos.LEFT);
                root.add(new Label(String.valueOf(Grammar.NonTerminals.get(i).data)), 0, i + 1);
            }
            for (int i = 0; i < Grammar.Terminals.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(Grammar.Terminals.get(i).data)), i + 1, 0);
            }

            for (int i = 0; i < Grammar.NonTerminals.size(); i++) {
                for (int j = 0; j < Grammar.Terminals.size(); j++) {
                    if (Grammar.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data).get(Grammar.Terminals.get(j).data) == null) {
                        GridPane.setHalignment(new Label(""), HPos.LEFT);
                        root.add(new Label(""), j + 1, i + 1);
                    } else {
                        GridPane.setHalignment(new Label(Grammar.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data).get(Grammar.Terminals.get(j).data).toString()), HPos.LEFT);
                        root.add(new Label(Grammar.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data).get(Grammar.Terminals.get(j).data).toString()), j + 1, i + 1);
                    }
                }
            }

            Scene scene = new Scene(root, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("Syntax matrix");
            newWindow.setScene(scene);

            newWindow.show();


        });

        LoadFile.setOnAction(actionEvent -> {
            Node source = (Node) actionEvent.getSource();
            Stage Stage = (Stage) source.getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtfilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtfilter);
            fileChooser.setTitle("File choosing");
            File fileObject = fileChooser.showOpenDialog(Stage);

            if(fileObject == null){
                LogConsole.appendText("Input error\n");
                return;
            }
            /*
            String str = "";
            try (Scanner scanner = new Scanner(fileObject)) {
                while (scanner.hasNext())
                    str += scanner.next() + "\n";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
             */

            try {
                Grammar = new Grammar(this, fileObject.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}