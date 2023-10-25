package org.example.queries;

import org.example.api.MovingItemDTO;

public class MovingItemDTOImpl implements MovingItemDTO {
    String name;
    int[] location;
    int moves;
    int value;


    public MovingItemDTOImpl(String name, int[] location, int moves, int value) {
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
