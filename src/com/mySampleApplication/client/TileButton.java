package com.mySampleApplication.client;

import com.google.gwt.user.client.ui.Button;

/**
 * Created by chrisjohnston on 12/18/14.
 */
public class TileButton extends Button {
    int row, column;
    TileButton(int r, int c, String v){
        super();
        row =r;
        column = c;
        this.setText(v);
    }
}
