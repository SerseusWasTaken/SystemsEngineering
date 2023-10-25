package org.example;

import org.example.api.Commands;
import org.example.api.EventStore;
import org.example.events.*;

public class CommandHandler implements Commands {

    EventStore eventStore;

    public CommandHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void createItem(String id) {
        eventStore.storeEvent(new CreateEvent(id));
    }

    @Override
    public void createItem(String id, int[] position, int value) {
        eventStore.storeEvent(new CreateEvent(id, position, value));
    }

    @Override
    public void deleteItem(String id) {
        eventStore.storeEvent(new DeleteEvent(id));
    }

    @Override
    public void moveItem(String id, int[] vector) {
        eventStore.storeEvent(new MoveEvent(id, vector));
    }

    @Override
    public void changeValue(String id, int newValue) {
        eventStore.storeEvent(new ChangeValueEvent(id, newValue));
    }
}
