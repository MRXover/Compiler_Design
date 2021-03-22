package main;

import automatons.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import stuff.Production;
import stuff.Token;

import java.util.ArrayList;
import java.util.Stack;

import static main.GrammarType.*;

public class Parser {
    private final GrammarType type;
    private final Grammar g;

    public Parser(GrammarType type, Grammar g){
        this.type = type;
        this.g = g;
    }

    public void Parse(ArrayList<Token> Input, LL_Automaton automaton) {
        int codePointer = 0;
        Stack<Token> stack = new Stack<>();
        stack.push(new Token("$"));
        stack.push(g.startSymbol);

        while(true){
            if(stack.peek().equals(new Token("$")) && codePointer == Input.size()){
                automaton.getController().LogConsole.appendText("Success\n");
                return;
            } else if(stack.peek().equals(new Token("#"))){
                stack.pop();
            } else if(codePointer < Input.size() && Input.get(codePointer).equals(stack.peek())){
                codePointer++;
                stack.pop();
            } else {
                if(codePointer == Input.size()){
                    if(automaton.TokenHasEpsProd(stack.peek()))
                        stack.pop();
                    continue;
                }
                Production p = automaton.SyntaxMatrix.get(stack.peek().data).get(Input.get(codePointer).data);
                if(p == null){
                    automaton.getController().LogConsole.appendText("FAIL\n");
                    return;
                }
                ArrayList<Token> temp = p.definition;
                stack.pop();
                for (int i = temp.size() - 1; i > -1; i--)
                    stack.push(temp.get(i));
            }
        }
    }

    public void Parse(ArrayList<Token> Input, Automaton automaton) {
        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("Строка"), 0, 0);
        root.add(new Label("Стек"), 1, 0);
        root.add(new Label("Символы"), 2, 0);
        root.add(new Label("Вход"), 3, 0);
        root.add(new Label("Действие"), 4, 0);

        ArrayList<Token> input = new ArrayList<>(Input);
        input.add(new Token("$", "END_MARKER"));

        ArrayList<Token> symbols = new ArrayList<>();
        symbols.add(new Token("$", "END_MARKER"));

        System.out.println(Input);

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        Token a = input.get(0);
        int pointer = 0;
        int stringIndex = 1;

        while (true) {
            int s = stack.peek();

            root.add(new Label("(" + stringIndex + ")"), 0, stringIndex);
            root.add(new Label(String.valueOf(stack)), 1, stringIndex);

            String symbolsString = "";
            for(Token t : symbols)
                symbolsString += t.data;

            root.add(new Label(symbolsString), 2, stringIndex);

            String inputString = "";
            for(Token t : input.subList(pointer, input.size()))
                inputString += t.data;
            root.add(new Label(inputString), 3, stringIndex);

            String action = "";
            switch(type){
                case SLR:
                    action = ((SLR_Automaton)automaton).ACTION(s, a);
                    break;
                case LR:
                    action = ((LR_Automaton)automaton).ACTION(s, a);
                    break;
                case LALR:
                    action = ((LALR_Automaton)automaton).ACTION(s, a);
                    break;
            }

            if ( action.charAt(0) == 's'){
                symbols.add(input.get(pointer));
                stack.push(Integer.valueOf(action.substring(1)));
                pointer++;
                a = input.get(pointer);
                System.out.println("Перенос в " + Integer.valueOf(action.substring(1)));
                root.add(new Label("Перенос в " + Integer.valueOf(action.substring(1))), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "Свертка по " + g.Productions.get(prodNumber).nonTerminal.data + " -> ";
                for (Token t : g.Productions.get(prodNumber).definition){
                    shift += t.data;
                    stack.pop();
                }
                System.out.println(shift);
                root.add(new Label(shift), 4, stringIndex);
                int count = g.Productions.get(prodNumber).definition.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                }
                symbols.add(g.Productions.get(prodNumber).nonTerminal);

                switch(type){
                    case SLR:
                        stack.push( ((SLR_Automaton)automaton).getIndexFromGOTO(((SLR_Automaton)automaton).items.get(stack.peek()),
                                g.Productions.get(prodNumber).nonTerminal));
                        break;
                    case LR:
                        stack.push( ((LR_Automaton)automaton).getIndexFromGOTO(((LR_Automaton)automaton).items.get(stack.peek()),
                                g.Productions.get(prodNumber).nonTerminal));
                        break;
                    case LALR:
                        stack.push( ((LALR_Automaton)automaton).getIndexFromGOTO( ((LALR_Automaton)automaton).items.get(stack.peek()),
                                g.Productions.get(prodNumber).nonTerminal));
                        break;
                }

            } else if(action.charAt(0) == 'a'){
                System.out.println("SUCCESS");
                root.add(new Label("SUCCESS"), 4, stringIndex);
                break;
            } else if(action.charAt(0) == 'e'){
                System.out.println("ERROR");
                root.add(new Label("ERROR"), 4, stringIndex);
                break;
            }
            stringIndex++;
        }

        ScrollPane scrollPane = new ScrollPane(root);

        Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
        newWindow.setTitle("LR Parse table");
        newWindow.setScene(scene);

        newWindow.show();
    }


}
