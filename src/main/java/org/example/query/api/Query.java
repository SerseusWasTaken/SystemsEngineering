package org.example.query.api;

import org.example.query.api.MovingItemDTO;

import java.util.Enumeration;


public interface Query {
    public MovingItemDTO getMovingItemByName(String name);
    public Enumeration<MovingItemDTO> getMovingItems();
    public Enumeration<MovingItemDTO> getMovingItemsAtPosition(int[] position);
}
