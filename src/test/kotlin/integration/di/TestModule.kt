package integration.di

import command.impl.CommandHandler
import command.impl.DomainModelImpl
import command.impl.EventStoreImpl
import handler.EventHandlerImpl
import io.mockk.spyk
import query.api.QueryModel
import query.impl.QueryDatabaseImpl
import query.impl.QueryModelImpl
import java.util.concurrent.LinkedBlockingDeque

object TestModule {

    val eventStore: EventStoreImpl = spyk(EventStoreImpl(LinkedBlockingDeque(), mutableListOf()))

    val queryDatabase: QueryDatabaseImpl = spyk(QueryDatabaseImpl(mutableMapOf()))

    val queryModel: QueryModel = spyk(QueryModelImpl(queryDatabase))

    val eventHandler: EventHandlerImpl = spyk(EventHandlerImpl(queryDatabase))

    val domainModel: DomainModelImpl = spyk(DomainModelImpl())

    val handler: CommandHandler = spyk(CommandHandler(domainModel))
}
