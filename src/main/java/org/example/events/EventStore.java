package org.example.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventStore {
    BlockingQueue<Event> queue = new LinkedBlockingQueue<>();

}
