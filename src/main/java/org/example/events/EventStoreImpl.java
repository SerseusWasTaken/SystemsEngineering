package org.example.events;

import org.example.api.EventStore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventStoreImpl implements EventStore {

    private BlockingQueue<Event> queue = new LinkedBlockingQueue<>();

    public void storeEvent(Event event) {
        queue.add(event);
    }
}
