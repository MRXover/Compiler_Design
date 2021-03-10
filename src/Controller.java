import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Controller {

    private Grammar Grammar;


    @FXML
    public TextArea LogConsole;
    @FXML
    public TextArea GrammarArea;
    @FXML
    public TextArea CodeArea;

    @FXML
    public Button Clean;
    @FXML
    public Button LoadGrammar;


    @FXML
    public Button LoadFile;
    @FXML
    private Button isLeftRecursive;
    @FXML
    private Button LeftFactoring;

    @FXML
    public Button SaveFile;


    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem LoadFromFile;
    @FXML
    private MenuItem MakeFirstAndFollow;
    @FXML
    private MenuItem MakeSyntaxMatrix;
    @FXML
    private MenuItem LLParse;
    @FXML
    private MenuItem TEST;
    @FXML
    private MenuItem MakeActionTable;
    @FXML
    private MenuItem SLR_Parse;

    @FXML
    void initialize() {

        Clean.setOnAction(actionEvent -> {
            GrammarArea.setText("");
            LogConsole.appendText("Grammar area was cleaned\n");
        });



        isLeftRecursive.setOnAction(actionEvent -> {
            if(Grammar == null){
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            LogConsole.appendText("" + Grammar.isLeftRecursive() + "\n");
        });


        LoadFile.setOnAction(actionEvent -> {
            Node source = (Node) actionEvent.getSource();
            Stage Stage = (Stage) source.getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtfilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtfilter);
            fileChooser.setTitle("File choosing");
            fileChooser.setInitialDirectory(new File("./"));
            File fileObject = fileChooser.showOpenDialog(Stage);

            if(fileObject == null){
                LogConsole.appendText("Input error\n");
                return;
            }

            try {
                Grammar = new Grammar(this, fileObject.getPath());
            } catch (Exception e) {
                LogConsole.appendText("Input error\n");
                e.printStackTrace();
            }
        });

        SaveFile.setOnAction(actionEvent -> {
            Node source = (Node) actionEvent.getSource();
            Stage Stage = (Stage) source.getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtfilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtfilter);
            fileChooser.setTitle("File saving");
            fileChooser.setInitialDirectory(new File("./"));

            File file = fileChooser.showSaveDialog(Stage);

            try(FileWriter writer = new FileWriter(file.getAbsoluteFile(), false)) {
                writer.write(GrammarArea.getText());
                writer.append('\n');
                writer.flush();
            } catch(IOException ex){
                System.out.println(ex.getMessage());
                LogConsole.appendText(ex.getMessage());
            }

        });

        LeftFactoring.setOnAction(actionEvent -> {
            if(Grammar == null){
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }

            if(!Grammar.isLeftRecursive()){
                LogConsole.appendText("Grammar is not left recursive\n");
                return;
            }

            LogConsole.appendText("BEFORE :\n");
            for(Production pro : Grammar.Productions)
                LogConsole.appendText(pro + "\n");

            while(Grammar.isLeftRecursive()){
                for(Production pro : Grammar.Productions){
                    if(pro.nonTerminal.data.equals(pro.get(0).data)) {
                        Token newToken = new Token(pro.nonTerminal.data + "\'");
                        Production newProd1 = new Production(pro.nonTerminal);
                        Production newProd2 = new Production(newToken);
                        Production newProd3 = new Production(newToken);

                        for(int i = 1; i < pro.definition.size(); i++){
                            newProd1.add(pro.definition.get(i));
                            newProd2.add(pro.definition.get(i));
                        }

                        newProd2.add(newToken);
                        newProd3.add(new Token("#", "EPSILON"));

                        Grammar.Productions.remove(pro);
                        Grammar.Productions.add(newProd1);
                        Grammar.Productions.add(newProd2);
                        Grammar.Productions.add(newProd3);
                        break;
                    }
                }
            }

            LogConsole.appendText("\nAFTER :\n");
            for(Production pro : Grammar.Productions)
                LogConsole.appendText(pro + "\n");
        });

        LoadGrammar.setOnAction(event -> {

        });

        LoadFromFile.setOnAction(actionEvent -> {
            Stage Stage = (Stage) menuBar.getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtfilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtfilter);
            fileChooser.setTitle("File choosing");
            fileChooser.setInitialDirectory(new File("./"));
            File fileObject = fileChooser.showOpenDialog(Stage);

            if(fileObject == null){
                LogConsole.appendText("Input error\n");
                return;
            }

            try {
                Grammar = new Grammar(this, fileObject.getPath());
            } catch (Exception e) {
                LogConsole.appendText("Input error\n");
                e.printStackTrace();
            }
        });


        //============================== LL ==============================//
        MakeFirstAndFollow.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            if(Grammar.isLeftRecursive()) {
                LogConsole.appendText("Grammar is Left Recursive\n");
                LogConsole.appendText("Please, make left factorization of your grammar\n");
                return;
            }
            try {
                Grammar.makeFirstSet();
                Grammar.makeFollowSet();

                Grammar.printFirstSet();
                System.out.println();
                Grammar.printFollowSet();
            } catch (Exception e){
                LogConsole.appendText(e.getMessage());
            }
        });

        MakeSyntaxMatrix.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            if(Grammar.FirstSet == null){
                Grammar.makeFirstSet();
                Grammar.makeFollowSet();
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

        LLParse.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            if(Grammar.FirstSet == null){
                Grammar.makeFirstSet();
                Grammar.makeFollowSet();
            }
            if(Grammar.SyntaxMatrix == null){
                Grammar.makeSyntaxMatrix();
            }
            Grammar.LL_Parse(Grammar.Lexer(CodeArea.getText()));
        });
        ///============================= LL ==============================//

        //============================== SLR =============================//

        MakeActionTable.setOnAction(event -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }

            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();

            Grammar.buildAllItems();

            Stage newWindow = new Stage();
            GridPane root = new GridPane();

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            int leftNonTerminalsPosition  = Grammar.Terminals.size();
            int rightNonTerminalsPosition = Grammar.NonTerminals.size() + Grammar.Terminals.size() - 1;

            for (int i = 0; i < Grammar.Terminals.size(); i++) {
                root.add(new Label(String.valueOf(Grammar.Terminals.get(i).data)), i+1, 0);
            }
            for (int i = leftNonTerminalsPosition; i < rightNonTerminalsPosition; i++) {
                root.add(new Label(String.valueOf(Grammar.NonTerminals.get(i - leftNonTerminalsPosition +1).data)), i + 1, 0);
            }


            for (int i = 0; i < Grammar.items.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(i)), 0, i+1);
            }

            // GOTO
            for (int i = 0; i < Grammar.items.size(); i++) {
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    int result = Grammar.getIndexFromGoTo(Grammar.items.get(i), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if( result == -1)
                        root.add(new Label(" "), j, i+1);
                    else
                        root.add(new Label(String.valueOf(result)), j, i+1);
                }
            }

            // ACTION
            Grammar.buildFollowLR();
            for (int i = 0; i < Grammar.items.size(); i++) {
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");

                    String result = Grammar.ACTION(i, a);
                    if(result.equals("Err"))
                        root.add(new Label(" "), j + 1, i+1);
                    else
                        root.add(new Label(result), j, i+1);
                }
            }

            Scene scene = new Scene(root, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("ACTION and GOTO table");
            newWindow.setScene(scene);

            newWindow.show();
        });

        SLR_Parse.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(Grammar.FollowLR == null)
                Grammar.buildFollowLR();
            if(Grammar.items == null)
                Grammar.buildAllItems();
            Grammar.SLRParser(Grammar.Lexer(CodeArea.getText()));
        });

        ///============================= SLR =============================//

        TEST.setOnAction(event -> {
            Grammar.makeFollowSet();
            Grammar.buildFollowLR();
            for(Token t : Grammar.NonTerminals){
                System.out.println(Grammar.follow.get(t));
                System.out.println(Grammar.FollowLR.get(t));
                System.out.println();
            }
        });
    }
}