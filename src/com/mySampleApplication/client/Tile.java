package com.mySampleApplication.client;

/**
 * Created by chrisjohnston on 12/18/14.
 */
public class Tile {
    boolean isFlagged;
    boolean isMine;
    boolean isUncovered;
    int row,column;
    int adjacentCount;
    Tile(){
        isFlagged = false;
        isMine = false;
        isUncovered = false;
        adjacentCount = 0;
        row = column = 0;
    }
    Tile(int r, int c){
        row = r;
        column = c;
        adjacentCount = 0;
        isFlagged = false;
        isMine = false;
        isUncovered = false;
    }
}
