package org.example.events;

public class CreateEvent extends Event {
    String name;
    int[] location = null;
    int value = 0;

    public CreateEvent(String name, int[] location, int value) {
        this.name = name;
        this.location = location;
        this.value = value;
    }

    public CreateEvent(String name){
        this.name = name;
    }
}
