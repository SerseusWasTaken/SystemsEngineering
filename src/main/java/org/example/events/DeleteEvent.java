package org.example.events;

public class DeleteEvent extends Event {
    String id;

    public DeleteEvent(String id) {
        this.id = id;
    }

}
