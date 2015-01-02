package com.mySampleApplication.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.DOM;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import javax.swing.*;
import java.util.ArrayList;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class MySampleApplication implements EntryPoint {

    Button easyButton = new Button("EASY");
    Button intermediateButton = new Button("INTERMEDIATE");
    Button hardButton = new Button("HARD");
    Button customButton = new Button("CUSTOM");
    String newName;
    Button nameCollectionButton = new Button("Enter name");
    TextBox customSize = new TextBox();
    TextBox customMines = new TextBox();
    TextBox nameCollector = new TextBox();
    ArrayList<Integer> diffHighScores;
    ArrayList<String> diffHighScoreNames;
    boolean firstClick = true;
    long startTime;
    Tile[][] logicGrid;
    FlexTable gameGrid = new FlexTable();
    VerticalPanel mainPanel;
    Label showMineCount;
    Difficulty difficulty;
    int minesLeft;
    boolean gameover;
    Storage highScores = Storage.getLocalStorageIfSupported();

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        highScores.clear();
        //Creating my panels;
        easyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDifficultyClick(event);
            }
        });
        intermediateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDifficultyClick(event);
            }
        });
        hardButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDifficultyClick(event);
            }
        });
        customButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDifficultyClick(event);
            }
        });
        mainPanel = new VerticalPanel();
        HorizontalPanel difficultyButtonsPanel = new HorizontalPanel();
        difficultyButtonsPanel.add(easyButton);
        difficultyButtonsPanel.add(intermediateButton);
        difficultyButtonsPanel.add(hardButton);
        difficultyButtonsPanel.add(customButton);
        difficultyButtonsPanel.add(customSize);
        difficultyButtonsPanel.add(customMines);
        showMineCount = new Label();
        difficultyButtonsPanel.add(showMineCount);
        mainPanel.add(gameGrid);
        RootPanel.get().add(difficultyButtonsPanel);
        RootPanel.get().add(mainPanel);
    }

    private void startNewGame(){
        gameover=false;
        highScores.setItem("En1","ABC");
        highScores.setItem("Es1","99");
        showMineCount.setText(String.valueOf(difficulty.bombs));
        firstClick = true;
        logicGrid = new Tile[difficulty.size][difficulty.size];

        gameGrid = new FlexTable();
        for(int r=0;r<difficulty.size;r++){
            for(int c=0;c<difficulty.size;c++){
                logicGrid[r][c] = new Tile(r,c);
            }
        }
        ClickHandler cellClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleCellClick(event);
            }
        };
        for(int r=0;r<difficulty.size;r++){
            gameGrid.insertRow(r);
            for(int c=0;c<difficulty.size;c++){
                logicGrid[r][c].adjacentCount = 0;
                gameGrid.addCell(r);

                Button btn = new TileButton(r,c," ");
                btn.addClickHandler(cellClickHandler);
                btn.setStyleName("cellButton");
                gameGrid.setWidget(r, c, btn);
            }
        }
        mainPanel.add(gameGrid);
    }

    private void handleDifficultyClick(ClickEvent e){
        mainPanel.remove(gameGrid);
        Widget sender = (Widget) e.getSource();
        if (sender == easyButton){
            difficulty = new Difficulty(9, 'E');
        }
        else if(sender == intermediateButton){
            difficulty = new Difficulty(13, 'I');
        }
        else if(sender == hardButton){
            difficulty = new Difficulty(18, 'H');
        }
        else if(sender == customButton){
            int s = Integer.parseInt(customSize.getText());
            if(customMines.getText().equalsIgnoreCase(""))difficulty = new Difficulty(s, 'c');
            int b = Integer.parseInt(customMines.getText());
            if(s<2)s=2;
            else if (s>40)s=40;
            if(b<0)b=0;
            else if(b>s*s)b = s*s - 1;
            difficulty = new Difficulty(s,'C',b);
        }
        startNewGame();
    }

    private void handleCellClick(ClickEvent e){
        if(gameover)return;
        boolean shifted = e.isShiftKeyDown();
        TileButton btn = (TileButton)e.getSource();
        int r = btn.row;
        int c = btn.column;
        Tile tile = logicGrid[r][c];
        if(tile.isUncovered){
            if(isChordable(tile)){
                chordTile(tile);
            }
            else{
                loseGame();
            }
        }else {
            if(firstClick){
                startTime = System.currentTimeMillis();
                tile.isUncovered = true;
                makeSomeMines();
                firstClick = false;
                if(tile.adjacentCount==0){
                    clearAdjacentZeros(tile);
                }else {
                    btn.setText(String.valueOf(tile.adjacentCount));
                    btn.setStyleName("cellButtonUncovered");
                }
            }else {

                if (shifted && !tile.isFlagged) {
                    tile.isFlagged = true;
                    btn.setText("$");
                    minesLeft--;
                    showMineCount.setText(String.valueOf(minesLeft));
                } else if(shifted){
                    tile.isFlagged = false;
                    minesLeft++;
                    showMineCount.setText(String.valueOf(minesLeft));
                    btn.setText("");
                } else if (tile.isMine) {
                    tile.isUncovered = true;
                    btn.setText("*");
                    btn.setStyleName("cellButtonUncovered");
                    loseGame();

                } else {
                    if(tile.adjacentCount==0){
                        clearAdjacentZeros(tile);
                    }
                    else {
                        btn.setText(String.valueOf(tile.adjacentCount));
                        btn.setStyleName("cellButtonUncovered");
                        tile.isUncovered = true;
                    }
                }
            }
        }
        gameGrid.setWidget(r,c,btn);
        isGameWon();
    }

    private void makeSomeMines(){
        int bombsLeft = difficulty.bombs;
        minesLeft = bombsLeft;
        while(bombsLeft > 0){
            int nextBomb = Random.nextInt(difficulty.size * difficulty.size);
            Tile tile = logicGrid[nextBomb % difficulty.size][nextBomb / difficulty.size];
            if((!tile.isUncovered) && (!tile.isMine)){
                tile.isMine = true;
                ArrayList<Tile> neighbs = getValidNeighbors(tile);
                for(int n=0;n<neighbs.size();n++){
                    neighbs.get(n).adjacentCount = countAdjacentBombs(new Tile(neighbs.get(n).row,neighbs.get(n).column));
                }
                tile.adjacentCount = countAdjacentBombs(new Tile(tile.row,tile.column));
                bombsLeft = bombsLeft-1;
            }
        }
    }

    public void isGameWon(){
        if(hasNoMoreSquares() && minesLeft == 0)winGame();
    }

    public void loseGame(){


        for(int r=0;r<difficulty.size;r++){
            for(int c=0;c<difficulty.size;c++){
                TileButton bt = (TileButton)gameGrid.getWidget(r,c);
                Tile tile = logicGrid[r][c];
                if(!tile.isUncovered && !tile.isFlagged){
                    tile.isUncovered = true;
                    if(!tile.isMine)bt.setText(String.valueOf(tile.adjacentCount));
                    else bt.setText("X");
                }
            }
        }

        Window.alert("You Lose! Time Elapsed: "+(System.currentTimeMillis() - startTime)/1000 + "s");
        gameover = true;
    }

    void winGame(){
        //TODO: high scores
        long duration = (System.currentTimeMillis() - startTime)/1000;
        Window.alert("You Win! Time Elapsed: "+duration + "s");
        //HIGH SCORES DISPLAYING AND SETTING
        // 'diff', 'Name or Score', 'places
        System.out.println("length of highScores: "+highScores.getLength());
        System.out.println(highScores.getItem(highScores.key(0)));
        System.out.println(difficulty.d);
        for(int k=0;k<highScores.getLength();k++){
            if(highScores.key(k).charAt(0)==difficulty.d){
                System.out.println("isEasy");
                if(highScores.key(k).charAt(1)=='n'){
                    diffHighScoreNames.set((int)highScores.key(k).charAt(2),highScores.getItem(highScores.key(k)));
                }
                else if(highScores.key(k).charAt(1)=='s')
                    diffHighScores.set((int)highScores.key(k).charAt(2), Integer.parseInt(highScores.getItem(highScores.key(k))));
            }
        }
        int n=0;
        System.out.println(diffHighScoreNames);
        System.out.println(diffHighScores);
        /*
        while(duration>diffHighScores.get(n)){
            n++;
        }
        */
        if(n<6){
            RootPanel.get().add(nameCollector);
            nameCollectionButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                newName = nameCollector.getText();
                RootPanel.get().remove(nameCollector);
                RootPanel.get().remove(nameCollectionButton);
                }
            });

            RootPanel.get().add(nameCollectionButton);

            diffHighScoreNames.add(n,newName);
            diffHighScores.add(n,(int)duration);
        }

        gameover = true;
    }

    public boolean hasNoMoreSquares(){
        for(int r=0;r<difficulty.size;r++){
            for(int c=0;c<difficulty.size;c++){
                if((!logicGrid[r][c].isUncovered && !logicGrid[r][c].isFlagged) || (!logicGrid[r][c].isMine && logicGrid[r][c].isFlagged)){
                    return false;
                }
            }
        }
        return true;
    }

    public int countAdjacentBombs(Tile t){
        ArrayList<Tile> neighbs = getValidNeighbors(t);
        int count = 0;
        for(int n=0;n< neighbs.size();n++){
            if(neighbs.get(n).isMine)count++;
        }
        return count;
    }

    private boolean isChordable(Tile t){
        ArrayList<Tile> neighbs = getValidNeighbors(t);
        for(int n=0;n<neighbs.size();n++){
            if((neighbs.get(n).isMine && !neighbs.get(n).isFlagged) || (neighbs.get(n).isFlagged && !neighbs.get(n).isMine)){
                return false;
            }
        }
        return true;
    }

    private void chordTile(Tile t){
        ArrayList<Tile> neighbs = getValidNeighbors(t);
        for(int n=0;n<neighbs.size();n++){
            if(!neighbs.get(n).isUncovered && !neighbs.get(n).isFlagged && !neighbs.get(n).isMine){
                if(neighbs.get(n).adjacentCount == 0){
                    clearAdjacentTiles(neighbs.get(n));
                }
                else{
                    neighbs.get(n).isUncovered = true;
                    TileButton bt = (TileButton)gameGrid.getWidget(neighbs.get(n).row,neighbs.get(n).column);
                    bt.setText(String.valueOf(neighbs.get(n).adjacentCount));
                    bt.setStyleName("cellButtonUncovered");
                }
            }
        }
    }



    void clearAdjacentZeros(Tile t){
        t.isUncovered = true;
        TileButton btn = (TileButton)gameGrid.getWidget(t.row,t.column);
        btn.setText(String.valueOf(t.adjacentCount));
        btn.setStyleName("cellButtonUncovered");
        //check every valid nearby square. call clearAdjacentZeros on any 0s found
        ArrayList<Tile> neighbs = getValidNeighbors(t);
        for(int n=0;n<neighbs.size();n++){
            if(!neighbs.get(n).isUncovered && !neighbs.get(n).isMine && !neighbs.get(n).isFlagged){
                if(neighbs.get(n).adjacentCount==0) {
                    clearAdjacentZeros(neighbs.get(n));
                }
                else {
                    clearAdjacentTiles(t);
                }
            }
        }
    }

    void clearAdjacentTiles(Tile t){
        t.isUncovered = true;
        TileButton btn = (TileButton)gameGrid.getWidget(t.row,t.column);
        btn.setText(String.valueOf(t.adjacentCount));
        btn.setStyleName("cellButtonUncovered");
        ArrayList<Tile> neighbors = getValidNeighbors(t);
        for(int n=0;n<neighbors.size();n++){
            if(!neighbors.get(n).isUncovered && !neighbors.get(n).isMine && !neighbors.get(n).isFlagged){
                if(neighbors.get(n).adjacentCount==0) {
                    clearAdjacentZeros(neighbors.get(n));
                }
                else {
                    neighbors.get(n).isUncovered = true;
                    TileButton bt = (TileButton)gameGrid.getWidget(neighbors.get(n).row,neighbors.get(n).column);
                    bt.setText(String.valueOf(neighbors.get(n).adjacentCount));
                    bt.setStyleName("cellButtonUncovered");
                }
            }
        }
    }



    public ArrayList<Tile> getValidNeighbors(Tile t){
        int r=t.row;
        int c=t.column;
        //print('r$r c$c');
        ArrayList<Tile> neighbors = new ArrayList<Tile>();
        if(r>0){
            if(c>0){
                neighbors.add(logicGrid[r-1][c-1]);
            }
            neighbors.add(logicGrid[r-1][c]);
        }
        if(c>0){
            neighbors.add(logicGrid[r][c-1]);
        }
        if(r<(difficulty.size-1)){
            neighbors.add(logicGrid[r+1][c]);
            if(c<(difficulty.size-1)){
                neighbors.add(logicGrid[r+1][c+1]);
            }
        }
        if(c<(difficulty.size-1)){
            neighbors.add(logicGrid[r][c+1]);
        }
        if(r>0&&c<(difficulty.size-1))neighbors.add(logicGrid[r-1][c+1]);
        if(r<(difficulty.size-1)&&c>0)neighbors.add(logicGrid[r+1][c-1]);
        //print('$cell n${neighbors.length}');
        return neighbors;
    }
}


class Difficulty{
    int size,bombs;
    char d;
    Difficulty(int s, char di){
        size = s;
        bombs = (int)((58.4) - (11.9*s) +(.7*s*s));
        d = di;
    }
    Difficulty(int s, char di, int b){
        size = s;
        bombs = b;
        d=di;
    }
}
