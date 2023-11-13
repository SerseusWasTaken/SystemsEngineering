package integration.di

import command.impl.CommandHandler
import command.api.DomainModel
import command.api.EventStore
import command.impl.DomainModelImpl
import command.impl.EventStoreImpl
import handler.EventHandler
import handler.EventHandlerImpl
import io.mockk.spyk
import query.api.QueryDatabase
import query.api.QueryModel
import query.impl.QueryDatabaseImpl
import query.impl.QueryModelImpl
import java.util.concurrent.LinkedBlockingDeque

object TestModule {

    val eventStore: EventStore = spyk(EventStoreImpl(LinkedBlockingDeque()))

    val queryDatabase: QueryDatabaseImpl = spyk(QueryDatabaseImpl(mutableMapOf()))

    val queryModel: QueryModel = spyk(QueryModelImpl(queryDatabase))

    val eventHandler: EventHandler = spyk(EventHandlerImpl(eventStore, queryDatabase))

    val domainModel: DomainModel = spyk(DomainModelImpl(eventStore))

    val handler: CommandHandler = spyk(CommandHandler(domainModel))
}