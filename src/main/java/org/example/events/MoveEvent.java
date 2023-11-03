package org.example.events;

public class MoveEvent extends Event{
    public String id = null;
    public int[] vector = null;

    public MoveEvent(String id, int[] vector){
        this.id = id;
        this.vector = vector;
    }
}
