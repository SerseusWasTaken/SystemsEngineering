package org.example.command.api;

import org.example.command.events.Event;

public interface EventStore {

    void storeEvent(Event event);

    Event pop() throws InterruptedException;
}
