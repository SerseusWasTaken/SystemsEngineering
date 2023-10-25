package org.example.events;

public class ChangeValueEvent extends Event {

    String id;
    int newValue;

    public ChangeValueEvent(String id, int newValue){
        this.id = id;
        this.newValue = newValue;
    }
}
