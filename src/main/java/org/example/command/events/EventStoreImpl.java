package org.example.command.events;

import org.example.command.api.EventStore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventStoreImpl implements EventStore {

    private BlockingQueue<Event> queue = new LinkedBlockingQueue<>();

    public void storeEvent(Event event) {
        queue.add(event);
    }
    public Event pop() throws InterruptedException {
        return queue.take();
    }

}
