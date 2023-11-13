package di

import command.impl.CommandHandler
import command.api.DomainModel
import command.api.EventStore
import command.impl.DomainModelImpl
import command.impl.EventStoreImpl
import handler.EventHandler
import handler.EventHandlerImpl
import query.api.QueryDatabase
import query.api.QueryModel
import query.impl.QueryDatabaseImpl
import query.impl.QueryModelImpl
import java.util.concurrent.LinkedBlockingDeque

object SystemModule {

    val eventStore: EventStore = EventStoreImpl(LinkedBlockingDeque())

    val queryDatabase: QueryDatabase = QueryDatabaseImpl(mutableMapOf())

    val queryModel: QueryModel = QueryModelImpl(queryDatabase)

    val eventHandler: EventHandler = EventHandlerImpl(eventStore, queryDatabase)

    val domainModel: DomainModel = DomainModelImpl(eventStore)

    val handler: CommandHandler = CommandHandler(domainModel)
}