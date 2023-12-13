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
import java.util.*
import environment
import kafka.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class EventHandlerImpl(
        private val eventStore: EventStore,
        private val queryDatabase: QueryDatabase
) : EventHandler {
    private val timestampList = mutableListOf<Long>()

    val props = Properties()
    val consumer = Consumer(props, listOf("allEvents"))

    init {
        props.setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        props.setProperty("security.protocol", "PLAINTEXT")
        props.setProperty("group.id", "group1")
    }


    override fun fetchEvent() {
        val d = 1.seconds
        val msg = consumer.getEvents()
        msg.forEach{
            println("Recieved msg: $it")
            handleEvent(it)
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

    override fun getAverageRecieveTime(): Double {
        return timestampList.average()
    }
}
