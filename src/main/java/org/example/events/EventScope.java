package org.example.events;

public class EventScope {
    private Class<? extends Event> eventType;
    private String id;

    public EventScope(Class<? extends Event> eventType, String id){
        this.eventType = eventType;
        this.id = id;
    }
}
