package di

import command.impl.CommandHandler
import command.api.DomainModel
import command.api.EventStore
import command.impl.DomainModelImpl
import command.impl.EventStoreImpl
import handler.EventHandler
import handler.EventHandlerImpl
import kafka.Producer
import query.api.QueryDatabase
import query.api.QueryModel
import query.impl.QueryDatabaseImpl
import query.impl.QueryModelImpl
import java.util.concurrent.LinkedBlockingDeque
import environment
import kafka.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig

object SystemModule {

    val eventStore: EventStore = EventStoreImpl(LinkedBlockingDeque(), mutableListOf())

    val queryDatabase: QueryDatabase = QueryDatabaseImpl(mutableMapOf())

    val queryModel: QueryModel = QueryModelImpl(queryDatabase)

    val eventHandler: EventHandler = EventHandlerImpl(queryDatabase, Consumer(listOf("allEvents")) {
        setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        setProperty("security.protocol", "PLAINTEXT")
        setProperty("group.id", "group1")
    })

    val domainModel: DomainModel = DomainModelImpl(Producer {
        setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        setProperty("security.protocol", "PLAINTEXT")
    }, Consumer(listOf("allEvents")) {
        setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        setProperty("group.id", "group2")
        setProperty("security.protocol", "PLAINTEXT")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    })

    val handler: CommandHandler = CommandHandler(domainModel)
}