package org.example.events;

public class MoveEvent extends Event{
    String id = null;
    int[] vector = null;

    public MoveEvent(String id, int[] vector){
        this.id = id;
        this.vector = vector;
    }
}
