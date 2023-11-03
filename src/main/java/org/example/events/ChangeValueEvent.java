package org.example.events;

public class ChangeValueEvent extends Event {

    public String id;
    public int newValue;

    public ChangeValueEvent(String id, int newValue){
        this.id = id;
        this.newValue = newValue;
    }
}
