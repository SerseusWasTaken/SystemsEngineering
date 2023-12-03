package handler

import command.api.EventStore
import command.events.*
import jakarta.jms.MessageConsumer
import jakarta.jms.Queue
import jakarta.jms.Session
import jakarta.jms.TextMessage
import org.apache.activemq.ActiveMQConnectionFactory
import query.api.QueryDatabase
import query.impl.MovingItemDTOImpl

class EventHandlerImpl(
        private val eventStore: EventStore,
        private val queryDatabase: QueryDatabase
) : EventHandler {
    val connectionFactory = ActiveMQConnectionFactory("tcp://localhost:61616")
    val connection = connectionFactory.createConnection("query", "query")
    val session: Session
    val destination: Queue
    val consumer: MessageConsumer
    init {
        connection.start()
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        destination = session.createQueue("MovingItems.Events")
        consumer = session.createConsumer(destination)
    }

    override fun fetchEvent() {
        val msg = consumer.receive()
        if (msg is TextMessage) {
            handleEvent(msg.text.deserializeToEvent())
        }
    }

    override fun handleEvent(event: Event): Unit =
            when (event) {
                is CreateEvent -> queryDatabase.createItem(MovingItemDTOImpl.fromMovingItem(event.item))
                is ChangeValueEvent -> queryDatabase.changeValue(event.id, event.newValue)
                is MoveEvent -> queryDatabase.moveLocation(event.id, event.vector)
                is DeleteEvent -> queryDatabase.deleteItem(event.id)
                is ReplaceEvent -> {
                    queryDatabase.deleteItem(event.id)
                    if (event.doCreateItem)
                        queryDatabase.createItem(MovingItemDTOImpl(event.id, event.vector, 0, event.value))
                    else
                        queryDatabase.moveLocation(event.itemToMove, event.vector)
                }
            }
}
