package org.example.queries;

import org.example.MovingItemImpl;
import org.example.api.MovingItem;
import org.example.api.MovingItemDTO;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class QueryModel {
    private Map<String, MovingItem> data = new HashMap<>();

    void createItem(String id, int[] location, int numberOfMoves, int value) {
        MovingItem itemToAdd = new MovingItemImpl(id, location, numberOfMoves, value);
        data.put(itemToAdd.getName(), itemToAdd);
    }

    void changeValue(String id, int value) {
        MovingItemImpl oldItem = (MovingItemImpl) data.get(id);
        MovingItemImpl newItem = new MovingItemImpl(oldItem.getName(),
                oldItem.getLocation(), oldItem.getNumberOfMoves(), value);

        data.replace(id, newItem);
    }

    public void moveLocation(String id, int[] vector) {
        MovingItemImpl oldItem = (MovingItemImpl) data.get(id);
        int[] newLocation = new int[oldItem.getLocation().length];
        for (int i = 0; i < oldItem.getLocation().length; i++) {
            newLocation[i] = oldItem.getLocation()[i] + vector[i];
        }
        MovingItemImpl newItem = new MovingItemImpl(oldItem.getName(),
                newLocation, oldItem.getNumberOfMoves() + 1, oldItem.getValue());
        data.replace(id, newItem);
    }

    void deleteItem(String id){
        data.remove(id);
    }

    MovingItemDTO getItem(String id) {
        MovingItem item = data.get(id);
        return new MovingItemDTOImpl(item.getName(), item.getLocation(), item.getNumberOfMoves(), item.getValue());
    }

    Enumeration<MovingItemDTO> getItemsAtPosition(int[] position) {
        var items = data.values().stream().filter(item -> item.getLocation() == position)
                .map(item -> (MovingItemDTO) new MovingItemDTOImpl(item.getName(), item.getLocation(), item.getNumberOfMoves(), item.getValue()))
                .toList();
        return Collections.enumeration(items);
    }

    Enumeration<MovingItemDTO> getMovingItems(){
        var items = data.values().stream()
                .map(item -> (MovingItemDTO) new MovingItemDTOImpl(item.getName(), item.getLocation(), item.getNumberOfMoves(), item.getValue()))
                .toList();
        return Collections.enumeration(items);
    }
}


