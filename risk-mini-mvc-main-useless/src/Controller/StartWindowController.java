package Controller;

import View.StartWindowView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StartWindowController {
    JFrame startWindowFrame;

    Color playerOneColor;
    Color playerTwoColor;
    Color playerThreeColor;
    Color playerFourColor;
    String boardChoice;


    private final List<String> playerNames = new ArrayList<>();

    public void setPlayerOneColor(Color color) {
        playerOneColor = color;
    }
    public void setPlayerTwoColor(Color color) {
        playerTwoColor = color;
    }
    public void setPlayerThreeColor(Color color) {
        playerThreeColor = color;
    }
    public void setPlayerFourColor(Color color) {
        playerFourColor = color;
    }
    public void setBoardChoice(String choice) {
        boardChoice = choice;
    }

    public boolean boardChosen() {
        return boardChoice != null;
    }
    public boolean colorsSet() {
        return playerOneColor != null && playerTwoColor != null;
    }

    public void setPlayerName(int index, String name) {
        if (index >= 0 && index < playerNames.size()) {
            playerNames.set(index, name);
        }
    }

    public String getPlayerName(int index) {
        if (index >= 0 && index < playerNames.size()) {
            return playerNames.get(index);
        }
        return null;
    }

    public void createStartWindow() {
        startWindowFrame = new StartWindowView(this).drawStartWindowFrame();
        for (int i = 0; i < 4; i++) {
            playerNames.add("");
        }
    }

    public void startGame() {

        BoardController board = new BoardController(boardChoice,
                getPlayerName(0), getPlayerName(1),
                playerOneColor, playerTwoColor,
                getPlayerName(2), getPlayerName(3),
                playerThreeColor, playerFourColor);

        board.createBoardView();

        startWindowFrame.dispose();

    }

    public static void main(String[] args) {
        StartWindowController startWindowController = new StartWindowController();
        startWindowController.createStartWindow();
    }
}

