package Controller;

import Model.Country;
import Config.NeighborRelations;
import Model.Player;
import View.BoardView;
import View.CountryView;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BoardController {

    BoardView boardView;

    String turn;
    private String phase = "Set Soldiers";
    String lastPhase;

    Map<String, Country> allCountries = new HashMap<>();
    Map<String, CountryView> allCountryViews = new HashMap<>();
    Map<String, String[]> countryNeighbors = new HashMap<>();

    private final String boardChoice;
    private final Player playerOne;
    private final Player playerTwo;
    private final Player playerThree;
    private final Player playerFour;
    private Player currentPlayer;

    private final List<Player> players;
    public FightController fightController;
    private SendArmyController sendArmyController;


    public BoardController(String boardChoice, String playerOneName, String playerTwoName,
                           Color playerOneColor, Color playerTwoColor, String playerThreeName, String playerFourName,
                           Color playerThreeColor, Color playerFourColor) {
        this.boardChoice = boardChoice;
        this.playerOne = new Player(playerOneName, playerOneColor);
        this.playerTwo = new Player(playerTwoName, playerTwoColor);
        this.playerThree = new Player(playerThreeName, playerThreeColor);
        this.playerFour = new Player(playerFourName, playerFourColor);
        this.players = new ArrayList<Player>(Arrays.asList(
                this.playerOne,
                this.playerTwo,
                this.playerThree,
                this.playerFour
        ));
        this.currentPlayer = players.getFirst();
    }


    public void createBoardView() {
        boardView = new BoardView(this.boardChoice, allCountries, this, allCountryViews);
        boardView.setVisible(true);
        boardView.setCurrentPhaseLabel(getPhase());
        turn = this.currentPlayer.getName() + "'s Turn";
        boardView.setPlayerTurnLabel(turn);

        setCountryNeighbors(boardChoice);

        this.fightController = new FightController(this, boardView);
        this.sendArmyController = new SendArmyController(this, boardView);
    }

    public String getPhase() {
        return phase;
    }
    public void setPhase(String newPhase) {
        phase = newPhase;
    }
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }
    public void setCurrentPlayer(Player newCurrentPlayer) {
        this.currentPlayer = newCurrentPlayer;
    }
    public Player getPlayerOne() {
        return this.playerOne;
    }
    public Player getPlayerTwo() {
        return this.playerTwo;
    }
    public Player getPlayerThree() {
        return this.playerThree;
    }
    public Player getPlayerFour() {
        return this.playerFour;
    }

    // Depending on the board, sets the all other countries which a country can attack or send soldiers to
    public void setCountryNeighbors(String boardChoice) {
        switch (boardChoice) {
            case "board1" -> NeighborRelations.addCountryNeighbors1(countryNeighbors);
            case "board2" -> NeighborRelations.addCountryNeighbors2(countryNeighbors);
            case "board3" -> NeighborRelations.addCountryNeighbors3(countryNeighbors);
        }
    }

    // Highlights all neighbors of a country
    public void showHideNeighbors(String countryName, boolean show) {
        String[] allNeighbors = countryNeighbors.get(countryName);

        for (String neighbor : allNeighbors) {
            allCountryViews.get(neighbor).setHighlight(show);
        }
    }

    // Checks if all available countries have been chosen at beginning of game
    public boolean allCountriesFilled() {
        boolean allFilled = true;
        for (Country c : allCountries.values()) {
            if (c.getOwner() == null) {
                allFilled = false;
                break;
            }
        }
        return allFilled;
    }

    public boolean checkIfNeighbor(String countryName, String potentialNeighbor) {
        boolean neighborCheck = false;
        String[] neighbors = countryNeighbors.get(countryName);
        for (String neighbor : neighbors) {
            if(neighbor.equals(potentialNeighbor)) {
                neighborCheck = true;
                break;
            }
        }
        return neighborCheck;
    }
    private void switchToNextPlayer() {
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
    }

    // Logic for the first phase, where both players choose and fill their starting countries
    // Logik angepasst so das der code sich nicht wiederholt
    // (Marc)
    public void placeSoldiers(Country country, CountryView view) {


        if ((country.getSoldiersInside() == 0 || allCountriesFilled()) && this.currentPlayer.getSoldiers() > 0) {

            country.setOwner(this.currentPlayer);
            this.currentPlayer.removeSoldiers(1);
            country.addSoldiersInside(1);
            view.setSoldierLabel("Soldiers: " + country.getSoldiersInside());
            view.setBackgroundColor(this.currentPlayer.getPlayerColor());
            view.setSoldierIcons(country.getSoldiersInside());
            switchToNextPlayer();
            turn = currentPlayer.getName() + "'s Turn";
            boardView.setPlayerTurnLabel(turn);

        }

        if (players.getLast().getSoldiers() == 0) {

            setPhase("Attack Phase");
            boardView.setCurrentPhaseLabel(getPhase());
            boardView.endTurnButton.setEnabled(true);

        }
    }

    // Setting or Unsetting an attacking and defending country for the Attack Phase
    public void attackPhase(Country country, CountryView view) {
        if(this.fightController.getAttackingCountry() == null && country.getOwner() == this.currentPlayer && country.getSoldiersInside() > 1) {

            this.fightController.setAttackingCountry(country);
            this.fightController.setAttackingCountryView(view);
            view.setBackgroundColor(Color.CYAN);

        } else if (this.fightController.getAttackingCountry() != null && this.fightController.getAttackingCountry().getName().equals(country.getName())) {

            this.fightController.setAttackingCountry(null);
            this.fightController.setAttackingCountryView(null);

            view.setBackgroundColor(this.currentPlayer.getPlayerColor());

            boardView.attackButton.setEnabled(false);

        } else if (this.fightController.getAttackingCountry() != null &&
                this.fightController.getDefendingCountry() == null &&
                checkIfNeighbor(this.fightController.getAttackingCountry().getName(), country.getName()) &&
                this.fightController.getAttackingCountry().getOwner() != country.getOwner()) {

            this.fightController.setDefendingCountry(country);
            this.fightController.setDefendingCountryView(view);
            view.setBackgroundColor(Color.RED);
            boardView.attackButton.setEnabled(true);

        } else if (this.fightController.getDefendingCountry() != null && this.fightController.getDefendingCountry().getName().equals(country.getName())) {

            this.fightController.setDefendingCountry(null);
            this.fightController.setDefendingCountryView(null);
            view.setBackgroundColor(country.getOwner().getPlayerColor());
            boardView.attackButton.setEnabled(false);

        }
    }

    // Start the extra phase for each player after they clicked their card button with 3 or more cards
    //Implemented Function so we only use it one time and dont repeat it 4 times (Marc)
    public void playerSetCardsPhase(Player player) {

        player.cardsToSoldiers();

        switch (players.indexOf(player)) {
            case 0 -> boardView.setPlayerOneCardsButtonText(player.getName() + " Cards: " + player.getCards());
            case 1 -> boardView.setPlayerTwoCardsButtonText(player.getName() + " Cards: " + player.getCards());
            case 2 -> boardView.setPlayerThreeCardsButtonText(player.getName() + " Cards: " + player.getCards());
            case 3 -> boardView.setPlayerFourCardsButtonText(player.getName() + " Cards: " + player.getCards());
        }

        lastPhase = getPhase();
        setPhase(player.getName() + ": Set Soldiers");
        boardView.setCurrentPhaseLabel(getPhase());
        boardView.endTurnButton.setEnabled(false);

    }

    // Logic for the extra phase, after the card button click
    //Here also trying to change it so we only need one function (Marc)

    public void playerSetCardTroops(Country country, CountryView view, Player player) {

        player.removeSoldiers(1);
        country.addSoldiersInside(1);
        view.setSoldierLabel("Soldiers: " + country.getSoldiersInside());
        view.setSoldierIcons(country.getSoldiersInside());

        if(player.getSoldiers() == 0) {

            setPhase(lastPhase);
            boardView.setCurrentPhaseLabel(getPhase());

        } else {

            boardView.setCurrentPhaseLabel(player.getName() + ": Set " + player.getSoldiers() + " Soldier(s)");

        }
    }


    // Setting / Unsetting a sending and receiving country and opening the Send Armies Window
    public void fortificationPhase(Country country, CountryView view) {

        if (this.sendArmyController.getSendingCountry() == null &&
                country.getSoldiersInside() > 1 &&
                country.getOwner() == this.currentPlayer) {

            this.sendArmyController.setSendingCountry(country);
            this.sendArmyController.setSendingCountryView(view);
            view.setBackgroundColor(Color.MAGENTA);


        } else if (this.sendArmyController.getSendingCountry() != null &&
                this.sendArmyController.getSendingCountry().getName().equals(country.getName())) {

            this.sendArmyController.setSendingCountry(null);
            this.sendArmyController.setSendingCountryView(null);


            view.setBackgroundColor(this.currentPlayer.getPlayerColor());


        } else if (this.sendArmyController.getReceivingCountry() == null &&
                this.sendArmyController.getSendingCountry() != null &&
                checkIfNeighbor(this.sendArmyController.getSendingCountry().getName(), country.getName()) &&
                this.sendArmyController.getSendingCountry().getOwner() == country.getOwner()) {

            this.sendArmyController.setReceivingCountry(country);
            this.sendArmyController.setReceivingCountryView(view);
            this.sendArmyController.createSendArmyView();
        }
    }

    // Sets new Soldiers at beginning of turn and switches to other player, when first one is done
    public void setNewTroops(Country country, CountryView view) {
        if (country.getOwner() == this.currentPlayer) {

            country.addSoldiersInside(1);
            this.currentPlayer.removeSoldiers(1);
            view.setSoldierLabel("Soldiers: " + country.getSoldiersInside());
            view.setSoldierIcons(country.getSoldiersInside());
            updatePhaseLabel();

            if (this.currentPlayer.getSoldiers() == 0) {
                    setPhase("Attack Phase");
                    boardView.setCurrentPhaseLabel(getPhase());
            }
        }
    }

    private void updatePhaseLabel() {
        boardView.setCurrentPhaseLabel(getPhase() + " " + this.currentPlayer.getName() + " " + this.currentPlayer.getSoldiers() + " Soldier(s)");
    }

    public void endPhase() {
        if(getPhase().equals("Attack Phase")) {
            setPhase("Fortification Phase");
            boardView.setCurrentPhaseLabel("Fortifications: " + this.sendArmyController.getFortifications() + " Left");
            boardView.setEndTurnButtonText("End Turn");
        }
        else if(getPhase().equals("Fortification Phase")) {
            endTurn();
        }
    }

    //funktion dynamisch angepasst um wiederholung zu vermeiden. (Marc)
    public void checkIfTooManyCards(Player player) {
        // If player has more than 5 cards, he is forced to use them
        if (player.getCards() > 5) {
            player.cardsToSoldiers();
            System.out.println(player);

            switch (players.indexOf(player)) {
                case 0 -> boardView.setPlayerOneCardsButtonText(player.getName() + " Cards: " + player.getCards());
                case 1 -> boardView.setPlayerTwoCardsButtonText(player.getName() + " Cards: " + player.getCards());
                case 2 -> boardView.setPlayerThreeCardsButtonText(player.getName() + " Cards: " + player.getCards());
                case 3 -> boardView.setPlayerFourCardsButtonText(player.getName() + " Cards: " + player.getCards());
            }
            setPhase(player.getName() + ": Set Soldiers");
            boardView.setCurrentPhaseLabel(player.getName() + ": Set " + player.getSoldiers() + " Soldier(s)");
        }
    }

    // Calculates the new Troops at the beginning of a turn, depending on owned countries and continents
    public void getNewTroops(Player player) {
        int newTroops = 0;
        int countriesOwned = 0;
        int continentsOwned = checkOwningContinents(player);

        for (Country c : allCountries.values()) {
            if(c.getOwner() == player) countriesOwned++;
        }
        if((countriesOwned / 3) < 3) {
            newTroops += 3;
        } else {
            newTroops += (int)Math.floor(countriesOwned / 3);
        }
        newTroops += continentsOwned * 3;

        player.addSoldiers(newTroops);
    }

    public int checkOwningContinents(Player player) {
        int continentsOwned = 0;
        int contA = 0;
        int contB = 0;
        int contC = 0;
        int contD = 0;

        for (Country c : allCountries.values()) {
            if(c.getOwner() == player) {
                if(c.getContinent().equals("A")) {
                    contA++;
                    if(contA == 6) continentsOwned++;
                }
                if(c.getContinent().equals("B")) {
                    contB++;
                    if(contB == 6) continentsOwned++;
                }
                if(c.getContinent().equals("C")) {
                    contC++;
                    if(contC == 6) continentsOwned++;
                }
                if(c.getContinent().equals("D")) {
                    contD++;
                    if(contD == 6) continentsOwned++;
                }
            }
        }
        return continentsOwned;
    }


    public void endTurn() {
        switchToNextPlayer();
        turn = this.currentPlayer.getName() + "'s Turn";
        boardView.setPlayerTurnLabel(turn);
        setPhase("New Troops Phase");
        getNewTroops(this.currentPlayer);
        boardView.setCurrentPhaseLabel(getPhase() + " " + this.currentPlayer.getName() + " " + this.currentPlayer.getSoldiers() + " Soldier(s)");
        this.sendArmyController.setFortifications(3);
    }

    public void checkWin() {
        boolean win = true;
        for(Country country : allCountries.values()) {
            if (country.getOwner() != currentPlayer) {
                win = false;
                break;
            }
        }
        if(win) {
            JOptionPane.showMessageDialog(boardView, currentPlayer.getName() + " has won the game!");
        }
    }


}
