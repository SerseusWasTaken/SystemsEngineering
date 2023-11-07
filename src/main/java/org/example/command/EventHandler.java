package org.example.command;

import org.example.command.api.EventStore;
import org.example.query.QueryModel;
import org.example.command.events.*;

//TODO: write implementation
public class EventHandler {

    private QueryModel queryModel;
    private EventStore eventStore;

    public EventHandler(QueryModel queryModel, EventStore store) {
        this.queryModel = queryModel;
        this.eventStore = store;
    }

    public void fetchEvent() throws InterruptedException {
        handleEvent(eventStore.pop());
    }

    public void handleEvent(Event event) {
        if (event instanceof ChangeValueEvent changeValueEvent) {
            queryModel.changeValue(changeValueEvent.id, changeValueEvent.newValue);
        } else if (event instanceof CreateEvent createEvent) {
            queryModel.createItem(createEvent.name, createEvent.location, 0, createEvent.value);
        } else if (event instanceof DeleteEvent deleteEvent) {
            queryModel.deleteItem(deleteEvent.id);
        } else if (event instanceof MoveEvent moveEvent) {
            queryModel.moveLocation(moveEvent.id, moveEvent.vector);
        }
    }
}
