package org.example.command;

import org.example.command.api.MovingItem;

public class MovingItemImpl implements MovingItem {
    String name;
    int[] location;
    int moves;
    int value;


    public MovingItemImpl(String name, int[] location, int moves, int value) {
        this.name = name;
        this.location = location;
        this.moves = moves;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int[] getLocation() {
        return location;
    }

    @Override
    public int getNumberOfMoves() {
        return moves;
    }

    @Override
    public int getValue() {
        return value;
    }
}
