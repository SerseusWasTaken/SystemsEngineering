package org.example.api;

import org.example.events.Event;

//TODO: write implementation
public interface EventStore {

    void storeEvent(Event event);
}
