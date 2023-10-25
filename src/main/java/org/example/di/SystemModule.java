package org.example.di;

import org.example.CommandHandler;
import org.example.api.EventStore;
import org.example.events.EventStoreImpl;
import org.example.queries.QueryModel;

public class SystemModule {

    private static final EventStore eventStore = new EventStoreImpl();

    private static final CommandHandler handler = new CommandHandler(eventStore);
    private static final QueryModel queryModel = new QueryModel();

    public static EventStore getEventStore() {
        return eventStore;
    }

    public static CommandHandler getHandler() {
        return handler;
    }
}
