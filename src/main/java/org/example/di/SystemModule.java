package org.example.di;

import org.example.command.CommandHandler;
import org.example.command.EventHandler;
import org.example.command.api.EventStore;
import org.example.query.domain.DomainModel;
import org.example.command.events.EventStoreImpl;
import org.example.query.QueryModel;

public class SystemModule {

    public static final QueryModel queryModel = new QueryModel();

    public static final EventStore eventStore = new EventStoreImpl();

    public static final EventHandler eventHandler = new EventHandler(queryModel, eventStore);

    public static final DomainModel domainModel = new DomainModel(eventStore);

    public static final CommandHandler handler = new CommandHandler(domainModel);
}
