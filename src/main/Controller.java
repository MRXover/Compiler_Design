package main;

import automatons.*;
import javafx.scene.layout.BorderPane;
import util.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.Token;

import java.io.*;
import java.util.*;

public class Controller {

    private Grammar Grammar;

    @FXML
    public TextArea LogConsole;
    @FXML
    public TextArea GrammarArea;
    @FXML
    public TextArea CodeArea;

    //================ Edit ================
    @FXML
    public MenuItem SaveAs;
    @FXML
    public MenuItem Clear;
    @FXML
    public CheckMenuItem GridForTables;

    //================ Left Factoring ================
    @FXML
    private MenuItem IsLeftRecursive;
    @FXML
    private MenuItem LeftFactorization;


    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem LoadFromFile;
    @FXML
    private MenuItem Quit;
    @FXML
    private MenuItem MakeFirstAndFollow;
    @FXML
    private MenuItem MakeSyntaxMatrix;
    @FXML
    private MenuItem LL_Parse;
    @FXML
    private MenuItem MakeActionTable;


    //================ Error Recovery ================
    @FXML
    public CheckMenuItem synchLL;
    @FXML
    public CheckMenuItem errorRecoveryLL;
    @FXML
    public CheckMenuItem errorRecoveryLR;

    @FXML
    private MenuItem SLR_Parse;
    @FXML
    private MenuItem ShowItemsSLR;

    @FXML
    private MenuItem ShowItemsLR;
    @FXML
    private MenuItem LR_MakeActionTable;
    @FXML
    private MenuItem LR_Parse;

    @FXML
    private MenuItem ShowItemsLALR;
    @FXML
    private MenuItem LALR_MakeActionTable;
    @FXML
    private MenuItem LALR_Parse;

    LL_Automaton   LL;
    SLR_Automaton  SLR;
    LR_Automaton   LR;
    LALR_Automaton LALR;

    @FXML
    void initialize() {

        Quit.setOnAction(event -> {
            System.exit(0);
        });

        Clear.setOnAction(actionEvent -> {
            GrammarArea.setText("");
            LogConsole.appendText("Grammar area was cleaned\n");
        });

        IsLeftRecursive.setOnAction(actionEvent -> {
            if(Grammar == null){
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            LogConsole.appendText("is Left Recursive: " + Grammar.isLeftRecursive() + "\n");
        });

        LoadFromFile.setOnAction(actionEvent -> {
            Stage Stage = (Stage) menuBar.getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtFilter);
            fileChooser.setTitle("File choosing");
            fileChooser.setInitialDirectory(new File("./"));
            File fileObject = fileChooser.showOpenDialog(Stage);
            if(fileObject == null){
                LogConsole.appendText("Input error\n");
                return;
            }
            try {
                Grammar = new Grammar(fileObject.getPath());
                //Grammar.isAugmented = true;
                String str = "";
                try (Scanner scanner = new Scanner(fileObject)) {
                    while (scanner.hasNextLine())
                        str += scanner.nextLine() + "\n";
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                GrammarArea.setText("");
                GrammarArea.appendText(str + "\n");
            } catch (Exception e) {
                LogConsole.appendText("Input error\n");
                e.printStackTrace();
            }
        });

        SaveAs.setOnAction(actionEvent -> {
            Stage Stage = (Stage)SaveAs.getParentPopup().getOwnerWindow();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TXT files(*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(txtFilter);
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

        LeftFactorization.setOnAction(actionEvent -> {
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
                LL = new LL_Automaton(Grammar, this);
                LL.makeFirstSet();
                LL.makeFollowSet();

                LL.printFirstSet();
                System.out.println();
                LL.printFollowSet();
            } catch (Exception e){
                LogConsole.appendText(e.getMessage());
            }
        });

        MakeSyntaxMatrix.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            if(LL == null)
                LL = new LL_Automaton(Grammar, this);
            if(LL.FirstSet == null){
                LL.makeFirstSet();
                LL.makeFollowSet();
            }

            LL.makeSyntaxMatrix();
            Stage newWindow = new Stage();
            GridPane root = new GridPane();
            if(GridForTables.isSelected())
                root.setGridLinesVisible(true);

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            for (int i = 0; i < Grammar.NonTerminals.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.NonTerminals[i])), HPos.LEFT);
                root.add(new Label(" " + String.valueOf(Grammar.NonTerminals.get(i).data) + " "), 0, i + 1);
            }
            for (int i = 0; i < Grammar.Terminals.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(" " + String.valueOf(Grammar.Terminals.get(i).data) + " "), i + 1, 0);
            }

            for (int i = 0; i < Grammar.NonTerminals.size(); i++) {
                for (int j = 0; j < Grammar.Terminals.size(); j++) {
                    if (LL.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data).get(Grammar.Terminals.get(j).data) == null) {
                        //GridPane.setHalignment(new Label(""), HPos.LEFT);
                        root.add(new Label(""), j + 1, i + 1);
                    } else {
                        //GridPane.setHalignment(new Label(" " + LL.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data)
                        //        .get(Grammar.Terminals.get(j).data).toString() + " "), HPos.LEFT);
                        root.add(new Label(" " + LL.SyntaxMatrix.get(Grammar.NonTerminals.get(i).data)
                                .get(Grammar.Terminals.get(j).data).toString() + " "), j + 1, i + 1);
                    }
                }
            }

            ScrollPane scrollPane = new ScrollPane(root);
            Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("Syntax matrix");
            newWindow.setScene(scene);
            newWindow.show();
        });

        LL_Parse.setOnAction(actionEvent -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }
            if(LL == null)
                LL = new LL_Automaton(Grammar, this);
            if(LL.FirstSet == null){
                LL.makeFirstSet();
                LL.makeFollowSet();
            }
            if(LL.SyntaxMatrix == null){
                LL.makeSyntaxMatrix();
            }
            try {
                new Parser(GrammarType.LL, Grammar).Parse(Grammar.Lexer(CodeArea.getText()),LL);
            } catch (Exception e){
                LogConsole.appendText("LL PARSING: FAIL\n");
                e.printStackTrace();
            }
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
            SLR = new SLR_Automaton(Grammar, this);
            SLR.buildAllItems();

            Stage newWindow = new Stage();
            GridPane root = new GridPane();
            if(GridForTables.isSelected())
                root.setGridLinesVisible(true);

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            int leftNonTerminalsPos  = Grammar.Terminals.size();
            int rightNonTerminalsPos = Grammar.NonTerminals.size() + Grammar.Terminals.size() - 1;

            for (int i = 0; i < Grammar.Terminals.size(); i++) {
                root.add(new Label(" " + String.valueOf(Grammar.Terminals.get(i).data)), i+1, 0);
            }
            for (int i = leftNonTerminalsPos; i < rightNonTerminalsPos; i++)
                root.add(new Label(" " + String.valueOf(
                        Grammar.NonTerminals.get(i - leftNonTerminalsPos +1).data)), i + 1, 0);

            for (int i = 0; i < SLR.items.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(" " + String.valueOf(i)), 0, i+1);
            }

            // GOTO
            for (int i = 0; i < SLR.items.size(); i++) {
                for (int j = leftNonTerminalsPos; j < rightNonTerminalsPos+1; j++) {
                    int result = SLR.getIndexFromGOTO(SLR.items.get(i),
                            Grammar.NonTerminals.get(j - leftNonTerminalsPos));
                    if( result == -1)
                        root.add(new Label(" "), j, i+1);
                    else
                        root.add(new Label(" " + String.valueOf(result)), j, i+1);
                }
            }

            // ACTION
            SLR.buildFollow();
            SLR.makeActionTable();
            for (int i = 0; i < SLR.items.size(); i++) {
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");
                    String result = SLR.actionTable.get(i).get(a);
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

        ShowItemsSLR.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(SLR == null)
                SLR = new SLR_Automaton(Grammar, this);
            if(SLR.items == null)
                SLR.buildAllItems();

            Stage newWindow = new Stage();
            BorderPane border = new BorderPane();
            TextArea root = new TextArea();

            int i = 0;
            for(ArrayList<Production> list : SLR.items){
                root.appendText("\n");
                root.appendText("I" + i + " =\n");
                for(Production pro : list) {
                    root.appendText("    " + pro.nonTerminal.data + " : ");
                    for(Token t : pro.definition)
                        root.appendText(t.data + " ");
                    root.appendText("\n");
                }
                i++;
            }

            newWindow.setX(200);
            newWindow.setY(100);

            border.setCenter(root);
            Scene scene = new Scene(border, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("SLR Items");
            newWindow.setScene(scene);
            newWindow.show();
        });

        SLR_Parse.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(SLR == null)
                SLR = new SLR_Automaton(Grammar, this);
            if(SLR.FollowLR == null)
                SLR.buildFollow();
            if(SLR.items == null)
                SLR.buildAllItems();
            new Parser(GrammarType.SLR, Grammar).Parse(Grammar.Lexer(CodeArea.getText()), SLR);
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

            LR = new LR_Automaton(Grammar);
            LR.buildAllItems();

            Stage newWindow = new Stage();
            GridPane root = new GridPane();
            if(GridForTables.isSelected())
                root.setGridLinesVisible(true);

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            int leftNonTerminalsPosition  = Grammar.Terminals.size();
            int rightNonTerminalsPosition = Grammar.NonTerminals.size() + Grammar.Terminals.size() - 1;

            for (int i = 0; i < Grammar.Terminals.size(); i++) {
                root.add(new Label(" " + String.valueOf(Grammar.Terminals.get(i).data)), i+1, 0);
            }
            for (int i = leftNonTerminalsPosition; i < rightNonTerminalsPosition; i++)
                root.add(new Label(" " + String.valueOf(
                        Grammar.NonTerminals.get(i - leftNonTerminalsPosition +1).data)), i + 1, 0);

            for (int i = 0; i < LR.items.size(); i++) {
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(String.valueOf(i)), 0, i+1);
            }

            // GOTO
            for (int i = 0; i < LR.items.size(); i++) {
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    int result = LR.getIndexFromGOTO(LR.items.get(i), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if( result == -1)
                        root.add(new Label(" "), j, i+1);
                    else
                        root.add(new Label(" " + result), j, i+1);
                }
            }

            // ACTION
            LR.makeActionTable();
            for (int i = 0; i < LR.items.size(); i++) {
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");
                    String result = LR.actionTable.get(i).get(a);
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
            if(LR == null)
                LR = new LR_Automaton(Grammar);
            if(LR.items == null)
                LR.buildAllItems();
            new Parser(GrammarType.LR, Grammar).Parse(Grammar.Lexer(CodeArea.getText()), LR);
        });

        ShowItemsLR.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(LR == null)
                LR = new LR_Automaton(Grammar);
            if(LR.items == null)
                LR.buildAllItems();

            Stage newWindow = new Stage();
            BorderPane border = new BorderPane();
            TextArea root = new TextArea();

            int i = 0;
            for(ArrayList<ItemLR> list : LR.items){
                root.appendText("\n");
                root.appendText("I" + i + " =\n");
                for(ItemLR item : list) {
                    root.appendText("    " + item.nonTerminal.data + " : ");
                    for(Token t : item.definition)
                        root.appendText(t.data + " ");
                    root.appendText(", " + item.terminal.data + "\n");
                }
                i++;
            }

            newWindow.setX(200);
            newWindow.setY(100);

            border.setCenter(root);
            Scene scene = new Scene(border, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("LR Items");
            newWindow.setScene(scene);
            newWindow.show();
        });

        ///============================= LR ==============================//


        ///============================ LALR ==============================//

        ShowItemsLALR.setOnAction(event -> {
            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();
            if(LALR == null){
                LR = new LR_Automaton(Grammar);
                LR.buildAllItems();

                LALR = new LALR_Automaton(LR);
                LALR.ResizeItems();
            }
            if(LALR.items == null){
                LALR.ResizeItems();
            }

            Stage newWindow = new Stage();
            BorderPane border = new BorderPane();
            TextArea root = new TextArea();

            for(Map.Entry<String, ArrayList<ItemLR>> pair : LALR.items.entrySet()){
                root.appendText("\n");
                root.appendText("I" + pair.getKey() + " =\n");
                for(ItemLR item : pair.getValue()) {
                    root.appendText("    " + item.nonTerminal.data + " : ");
                    for(Token t : item.definition)
                        root.appendText(t.data + " ");
                    root.appendText(", " + item.terminal.data + "\n");
                }
                System.out.println(pair.getKey() + " " + pair.getValue());
            }

            newWindow.setX(200);
            newWindow.setY(100);

            border.setCenter(root);
            Scene scene = new Scene(border, root.getMaxWidth(), root.getMaxHeight());
            newWindow.setTitle("LALR Items");
            newWindow.setScene(scene);
            newWindow.show();
        });

        LALR_MakeActionTable.setOnAction(event -> {
            if(Grammar == null) {
                LogConsole.appendText("Grammar is not loaded\n");
                return;
            }

            if(!Grammar.isAugmented)
                Grammar.augmentGivenGrammar();

            LR = new LR_Automaton(Grammar);
            LR.buildAllItems();

            LALR = new LALR_Automaton(LR);
            LALR.ResizeItems();

            Stage newWindow = new Stage();
            GridPane root = new GridPane();
            if(GridForTables.isSelected())
                root.setGridLinesVisible(true);

            newWindow.setX(200);
            newWindow.setY(100);

            root.setPadding(new Insets(20));
            root.setHgap(25);
            root.setVgap(15);

            int leftNonTerminalsPosition  = Grammar.Terminals.size();
            int rightNonTerminalsPosition = Grammar.NonTerminals.size() + Grammar.Terminals.size() - 1;

            for (int i = 0; i < Grammar.Terminals.size(); i++)
                root.add(new Label(" " + String.valueOf(Grammar.Terminals.get(i).data)), i+1, 0);
            for (int i = leftNonTerminalsPosition; i < rightNonTerminalsPosition; i++)
                root.add(new Label(" " + String.valueOf(
                        Grammar.NonTerminals.get(i - leftNonTerminalsPosition +1).data)), i + 1, 0);

            int ind = 0;
            for(Map.Entry<String, ArrayList<ItemLR>> pair : LALR.items.entrySet()){
                //GridPane.setHalignment(new Label(String.valueOf(Grammar.Terminals.get(i))), HPos.LEFT);
                root.add(new Label(" " + String.valueOf(pair.getKey())), 0, ind+1);
                ind++;
            }

            // GOTO
            ind = 0;
            for(Map.Entry<String, ArrayList<ItemLR>> pair : LALR.items.entrySet()){
                for (int j = leftNonTerminalsPosition; j < rightNonTerminalsPosition+1; j++) {
                    String result = LALR.getIndexFromGOTO(pair.getValue(), Grammar.NonTerminals.get(j - leftNonTerminalsPosition));
                    if(result.equals("-1"))
                        root.add(new Label(" "), j, ind+1);
                    else
                        root.add(new Label(" " + result), j, ind+1);
                }
                ind++;
            }

            // ACTION
            ind = 0;
            for(Map.Entry<String, ArrayList<ItemLR>> pair : LALR.items.entrySet()){
                for (int j = 1; j < Grammar.Terminals.size() + 1; j++) {
                    Token a;
                    if(j != Grammar.Terminals.size())
                        a = Grammar.Terminals.get(j - 1);
                    else
                        a = new Token("$", "END_MARKER");

                    String result = LALR.ACTION(pair.getKey(), a);
                    if(result.equals("err"))
                        root.add(new Label(" "), j + 1, ind+1);
                    else
                        root.add(new Label(" " + result), j, ind+1);
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
            if(LR == null) {
                LR = new LR_Automaton(Grammar);
                LR.buildAllItems();

                LALR = new LALR_Automaton(LR);
                LALR.ResizeItems();
            }

            new Parser(GrammarType.LALR, Grammar).Parse(Grammar.Lexer(CodeArea.getText()), LALR);
        });


        ///============================ LALR ==============================//


    }


}