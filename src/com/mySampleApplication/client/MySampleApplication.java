package com.mySampleApplication.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
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

    public static Button easyButton = new Button("EASY");
    public static boolean firstClick = true;
    public static Button intermediateButton = new Button("INTERMEDIATE");
    public static Button hardButton = new Button("HARDest");
    public Tile[][] logicGrid;
    FlexTable gameGrid = new FlexTable();
    VerticalPanel mainPanel;
    Label showMineCount;
    public static Difficulty difficulty;
    PopupPanel p;
    int minesLeft;
    boolean gameover;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        //Creating my panels);
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
        mainPanel = new VerticalPanel();
        HorizontalPanel difficultyButtonsPanel = new HorizontalPanel();
        difficultyButtonsPanel.add(easyButton);
        difficultyButtonsPanel.add(intermediateButton);
        difficultyButtonsPanel.add(hardButton);
        showMineCount = new Label();
        difficultyButtonsPanel.add(showMineCount);


        mainPanel.add(gameGrid);
        RootPanel.get().add(difficultyButtonsPanel);
        RootPanel.get().add(mainPanel);


    }



    private static class MyAsyncCallback implements AsyncCallback<String> {
        private Label label;

        public MyAsyncCallback(Label label) {
            this.label = label;
        }

        public void onSuccess(String result) {
            label.getElement().setInnerHTML(result);
        }

        public void onFailure(Throwable throwable) {
            label.setText("Failed to receive answer from server!");
        }
    }

    private void startNewGame(){
        gameover=false;
        showMineCount.setText(String.valueOf(difficulty.bombs));
        firstClick = true;
        logicGrid = new Tile[difficulty.size][difficulty.size];

        gameGrid = new FlexTable();
        for(int r=0;r<difficulty.size;r++){
            for(int c=0;c<difficulty.size;c++){
                logicGrid[r][c] = new Tile(r,c);
                gameGrid.getFlexCellFormatter().setStyleName(r,c,"flexTable");
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
        if (sender == MySampleApplication.easyButton){
            MySampleApplication.difficulty = new Difficulty(7);
            System.out.println(MySampleApplication.difficulty.size);
        }
        else if(sender == MySampleApplication.intermediateButton){
            MySampleApplication.difficulty = new Difficulty(10);
            System.out.println(MySampleApplication.difficulty.size);
        }
        else if(sender == MySampleApplication.hardButton){
            MySampleApplication.difficulty = new Difficulty(15);
            System.out.println(MySampleApplication.difficulty.size);
        }
        startNewGame();
    }

    private void handleCellClick(ClickEvent e){
        if(gameover)return;
        boolean shifted = e.isShiftKeyDown();
        TileButton btn = (TileButton)e.getSource();
        System.out.println(""+btn.row+" "+btn.column);
        int r = btn.row;
        int c = btn.column;
        Tile tile = logicGrid[r][c];
        if(tile.isUncovered){
            if(isChordable(tile)){
                System.out.println("chordable");
                chordTile(tile);
            }
            else{
                loseGame();
            }
        }else {
            if(firstClick){
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
        System.out.println("won?");
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

        Window.alert("You Lose!");
        gameover = true;
    }

    void winGame(){
        Window.alert("You Win!");
        gameover = true;
    }

    public boolean hasNoMoreSquares(){
        System.out.println("sq?");
        for(int r=0;r<difficulty.size;r++){
            for(int c=0;c<difficulty.size;c++){
                if((!logicGrid[r][c].isUncovered && !logicGrid[r][c].isFlagged) || (!logicGrid[r][c].isMine && logicGrid[r][c].isFlagged)){
                    System.out.println(""+r+" "+c);
                    return false;
                }
            }
        }
        System.out.println("nomoresquares");
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
        System.out.println("chord?");
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
            System.out.print(""+neighbs.get(n).row+" "+neighbs.get(n).column+" ");
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
    Difficulty(int s){
        size = s;
        bombs = (s*s)/6;
    }

}
