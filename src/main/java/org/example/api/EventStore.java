package org.example.api;

import org.example.events.Event;
import org.example.events.EventScope;

import java.util.Enumeration;

public interface EventStore {

    void storeEvent(Event event);

    Event pop() throws InterruptedException;
}
