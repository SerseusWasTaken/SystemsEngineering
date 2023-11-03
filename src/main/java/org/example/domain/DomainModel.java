package org.example.domain;

import org.example.api.EventStore;
import org.example.events.ChangeValueEvent;
import org.example.events.CreateEvent;
import org.example.events.DeleteEvent;
import org.example.events.MoveEvent;

import java.util.ArrayList;
import java.util.List;

public class DomainModel {
    private List<String> domainModel = new ArrayList<>();
    EventStore eventStore;

    public DomainModel(EventStore store) {
        this.eventStore = store;
    }
    
    private boolean isIdInUse(String id) {
        return domainModel.contains(id);
    }
    
    public void createItem(String id) {
        if (!isIdInUse(id)) {
            eventStore.storeEvent(new CreateEvent(id));
            domainModel.add(id);
        }
        else
            throw new IllegalArgumentException();
    }
    
    public void createItem(String id, int[] position, int value) {
        if (!isIdInUse(id)) {
            eventStore.storeEvent(new CreateEvent(id, position, value));
            domainModel.add(id);
        }
        else 
            throw new IllegalArgumentException();
    }
    
    public void deleteItem(String id) {
        if (isIdInUse(id)) {
            eventStore.storeEvent(new DeleteEvent(id));
            domainModel.remove(id);
        }
        else 
            throw new IllegalArgumentException();
    }
    
    public void moveItem(String id, int[] vector) {
        if (isIdInUse(id))
            eventStore.storeEvent(new MoveEvent(id, vector));
        else 
            throw new IllegalArgumentException();
    }
    
    public void changeValue(String id, int newValue) {
        if (isIdInUse(id))
            eventStore.storeEvent(new ChangeValueEvent(id, newValue));
        else
            throw new IllegalArgumentException();
    }
}
