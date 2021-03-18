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
import java.util.ArrayList;
import java.util.Map;

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
    private MenuItem LR_MakeActionTable;
    @FXML
    private MenuItem LR_Parse;

    @FXML
    private MenuItem LALR_MakeActionTable;
    @FXML
    private MenuItem LALR_Parse;

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


            for (int i = 0; i < Grammar.SLR_items.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(i)), 0, i+1);
            }

            // GOTO
            for (int i = 0; i < Grammar.SLR_items.size(); i++) {
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    int result = Grammar.getIndexFromGoTo(Grammar.SLR_items.get(i), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if( result == -1)
                        root.add(new Label(" "), j, i+1);
                    else
                        root.add(new Label(String.valueOf(result)), j, i+1);
                }
            }

            // ACTION
            Grammar.buildFollowLR();
            for (int i = 0; i < Grammar.SLR_items.size(); i++) {
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");

                    String result = Grammar.ACTION(i, a);
                    if(result.equals("err"))
                        root.add(new Label(" "), j + 1, i+1);
                    else
                        root.add(new Label(result), j, i+1);
                }
            }

            ScrollPane scrollPane = new ScrollPane(root);
            Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("ACTION and GOTO table");
            newWindow.setScene(scene);

            newWindow.show();
        });

        SLR_Parse.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(Grammar.FollowLR == null)
                Grammar.buildFollowLR();
            if(Grammar.SLR_items == null)
                Grammar.buildAllItems();
            Grammar.SLR_Parser(Grammar.Lexer(CodeArea.getText()));
        });

        ///============================= SLR =============================//

        //============================== LR ==============================//

        LR_MakeActionTable.setOnAction(event -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }

            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();

            Grammar.LR_buildAllItems();

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


            for (int i = 0; i < Grammar.LR_items.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(i)), 0, i+1);
            }


            // GOTO
            for (int i = 0; i < Grammar.LR_items.size(); i++) {
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    int result = Grammar.LR_getIndexFromGoTo(Grammar.LR_items.get(i), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if( result == -1)
                        root.add(new Label(" "), j, i+1);
                    else
                        root.add(new Label(String.valueOf(result)), j, i+1);
                }
            }

            // ACTION
            for (int i = 0; i < Grammar.LR_items.size(); i++) {
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");

                    String result = Grammar.LR_ACTION(i, a);
                    if(result.equals("err"))
                        root.add(new Label(" "), j + 1, i+1);
                    else
                        root.add(new Label(result), j, i+1);
                }
            }

            ScrollPane scrollPane = new ScrollPane(root);
            Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("ACTION and GOTO table");
            newWindow.setScene(scene);

            newWindow.show();
        });

        LR_Parse.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(Grammar.LR_items == null)
                Grammar.LR_buildAllItems();
            Grammar.LR_Parser(Grammar.Lexer(CodeArea.getText()));
        });

        ///============================= LR ==============================//


        ///============================ LALR ==============================//
        LALR_MakeActionTable.setOnAction(event -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }

            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();

            Grammar.LR_buildAllItems();
            Grammar.LALR_ResizeItems();

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


            int ind = 0;
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : Grammar.LALR_items.entrySet()){
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(pair.getKey())), 0, ind+1);
                ind++;
            }


            // GOTO
            ind = 0;
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : Grammar.LALR_items.entrySet()){
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    int result = Grammar.LALR_getIndexFromGoTo(pair.getValue(), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if( result == -1)
                        root.add(new Label(" "), j, ind+1);
                    else
                        root.add(new Label(String.valueOf(result)), j, ind+1);
                }
                ind++;
            }

            // ACTION
            ind = 0;
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : Grammar.LALR_items.entrySet()){
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");

                    String result = Grammar.LALR_ACTION(pair.getKey(), a);
                    if(result.equals("err"))
                        root.add(new Label(" "), j + 1, ind+1);
                    else
                        root.add(new Label(result), j, ind+1);
                }
                ind++;
            }


            ScrollPane scrollPane = new ScrollPane(root);
            Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("ACTION and GOTO table");
            newWindow.setScene(scene);

            newWindow.show();
        });

        LALR_Parse.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(Grammar.LR_items == null)
                Grammar.LR_buildAllItems();
            Grammar.LALR_Parser(Grammar.Lexer(CodeArea.getText()));
        });


        ///============================ LALR ==============================//

        TEST.setOnAction(event -> {
            try {
                Grammar = new Grammar(this, "example11.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Grammar.augmentGivenGrammar();
            Grammar.printGrammar();
            ItemLR i = new ItemLR(0, Grammar.Productions.get(0));
            i.setTerminal(new Token("$"));
            System.out.println(i);

            System.out.println(Grammar.LR_CLOSURE(i));
        });
    }
}