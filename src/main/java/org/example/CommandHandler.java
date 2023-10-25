package org.example;

import org.example.api.Commands;

public class CommandHandler implements Commands {

    @Override
    public void createItem(String id) {
    }

    @Override
    public void createItem(String id, int[] position, int value) {
        eventStore.storeEvent(new CreateEvent(id, position, value));
    }

    @Override
    public void deleteItem(String id) {

    }

    @Override
    public void moveItem(String id, int[] vector) {

    }

    @Override
    public void changeValue(String id, int newValue) {

    }
}
