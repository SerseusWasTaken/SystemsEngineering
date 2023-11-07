package org.example.command.events;

public class DeleteEvent extends Event {
    public String id;

    public DeleteEvent(String id) {
        this.id = id;
    }

}
