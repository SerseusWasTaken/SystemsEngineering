package org.example.di;

import org.example.CommandHandler;
import org.example.EventHandler;
import org.example.api.EventStore;
import org.example.domain.DomainModel;
import org.example.events.EventStoreImpl;
import org.example.queries.QueryModel;

public class SystemModule {

    public static final QueryModel queryModel = new QueryModel();

    public static final EventStore eventStore = new EventStoreImpl();

    public static final EventHandler eventHandler = new EventHandler(queryModel, eventStore);

    public static final DomainModel domainModel = new DomainModel(eventStore);

    public static final CommandHandler handler = new CommandHandler(domainModel);
}
