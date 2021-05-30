package main;

import automatons.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import util.Production;
import util.Token;

import java.util.ArrayList;
import java.util.Stack;

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

        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("Строка"), 0, 0);
        root.add(new Label("Стек"), 1, 0);
        root.add(new Label("Вход"), 2, 0);
        root.add(new Label("Действие"), 3, 0);

        int stringIndex = 1;
        if (Input.size() == 0){
            automaton.getController().LogConsole.appendText("111LL PARSING: FAIL\n");
            return;
        }

        while(true){
            root.add(new Label("(" + stringIndex + ")"), 0, stringIndex);
            String st = "";
            for (Token t : stack)
                st += t.data + " ";
            root.add(new Label(st), 1, stringIndex);

            String symbols = "";
            for(Token t : Input.subList(codePointer, Input.size()))
                symbols += t.data;
            root.add(new Label(symbols + "$"), 2, stringIndex);

            if(stack.peek().equals(new Token("$")) && codePointer == Input.size()){
                automaton.getController().LogConsole.appendText("LL PARSING: SUCCESS\n");
                root.add(new Label("SUCCESS"), 3, stringIndex);
                break;

            } else if(codePointer < Input.size() && Input.get(codePointer).equals(stack.peek())){
                root.add(new Label("Соответствие " + stack.peek().data), 3, stringIndex);
                codePointer++;
                stack.pop();
            } else if(stack.peek().equals(new Token("#"))){
                root.add(new Label("Пропуск пустого символа"), 3, stringIndex);
                stack.pop();
            } else {
                if(codePointer == Input.size()){
                    root.add(new Label("Вывод " + stack.peek().data + " : #"), 3, stringIndex);
                    if(automaton.TokenHasEpsProd(stack.peek()))
                        stack.pop();
                    else{
                        automaton.getController().LogConsole.appendText("LL PARSING: FAIL\n");
                        root.add(new Label("ERROR"), 3, stringIndex + 1);
                        break;
                    }
                    stringIndex++;
                    continue;
                }
                Production p = automaton.SyntaxMatrix.get(stack.peek().data).get(Input.get(codePointer).data);
                if(p == null){
                    automaton.getController().LogConsole.appendText("LL PARSING: FAIL\n");
                    root.add(new Label("ERROR"), 3, stringIndex);
                    break;
                }
                if(p.nonTerminal.type.equals("synch")){
                    if(stack.size() == 2){
                        root.add(new Label("Ошибка, пропускаем " + Input.get(codePointer).data +
                                "\n" + Input.get(codePointer + 1).data + " ∈ First (" + stack.peek().data + ")"),3,stringIndex);
                        codePointer++;
                    } else {
                        root.add(new Label("Ошибка, M[" + stack.peek().data + ", " + Input.get(codePointer).data + "] = synch\n" +
                                stack.peek().data + " снимается со стека"),3,stringIndex);
                        stack.pop();
                    }
                } else {
                    ArrayList<Token> temp = p.definition;
                    stack.pop();
                    for (int i = temp.size() - 1; i > -1; i--)
                        stack.push(temp.get(i));
                    root.add(new Label("Вывод " + p), 3, stringIndex);
                }

            }
            stringIndex++;
        }

        ScrollPane scrollPane = new ScrollPane(root);
        Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
        newWindow.setTitle("LL Parsing table");
        newWindow.setScene(scene);
        newWindow.show();
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
                    action = ((LALR_Automaton)automaton).ACTION("" + s, a);
                    break;
            }

            System.out.println("ACTION( " + s + ", " + a.data + ") = " + action);
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

                if(type == GrammarType.SLR)
                    stack.push( ((SLR_Automaton)automaton).getIndexFromGOTO(((SLR_Automaton)automaton).items.get(stack.peek()),
                            g.Productions.get(prodNumber).nonTerminal));

                else
                    stack.push( ((LR_Automaton)automaton).getIndexFromGOTO(((LR_Automaton)automaton).items.get(stack.peek()),
                            g.Productions.get(prodNumber).nonTerminal));


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

    public void Parse(ArrayList<Token> Input, LALR_Automaton automaton) {
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

        Stack<String> stack = new Stack<>();
        stack.push("0");

        Token a = input.get(0);
        int pointer = 0;
        int stringIndex = 1;

        while (true) {
            String s = stack.peek();

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

            String action = automaton.ACTION(s, a);


            System.out.println("ACTION( " + s + ", " + a.data + ") = " + action);
            if ( action.charAt(0) == 's'){
                symbols.add(input.get(pointer));
                stack.push(action.substring(1));
                pointer++;
                a = input.get(pointer);
                System.out.println("Перенос в " + action.substring(1));
                root.add(new Label("Перенос в " + action.substring(1)), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "Свертка по " + g.Productions.get(prodNumber).nonTerminal.data + " -> ";
                for (Token t : g.Productions.get(prodNumber).definition){
                    shift += t.data;
                    //if(!stack.isEmpty())
                    stack.pop();
                }
                System.out.println(shift);
                root.add(new Label(shift), 4, stringIndex);
                int count = g.Productions.get(prodNumber).definition.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                }
                symbols.add(g.Productions.get(prodNumber).nonTerminal);
                stack.push( automaton.getIndexFromGOTO( automaton.items.get(stack.peek()),
                                g.Productions.get(prodNumber).nonTerminal));

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
        newWindow.setTitle("LALR Parse table");
        newWindow.setScene(scene);
        newWindow.show();
    }



}
