package command.impl

import command.api.DomainModel
import command.api.EventStore
import command.api.MovingItemImpl
import command.events.*
import jakarta.jms.MessageProducer
import jakarta.jms.Queue
import jakarta.jms.Session
import org.apache.activemq.ActiveMQConnectionFactory
import query.utils.addValues

class DomainModelImpl(
    val eventStore: EventStore,
) : DomainModel {
    // This stuff probably has to get moved to the command handler
    val connectionFactory = ActiveMQConnectionFactory("tcp://localhost:61616")
    val connection = connectionFactory.createConnection("command", "command")
    val session: Session
    val destination: Queue
    val producer: MessageProducer
    init {
        connection.start()
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        destination = session.createQueue("MovingItems.Events")
        producer = session.createProducer(destination)
        producer.timeToLive = 2000
    }

    private fun executeWhenIdIsNotInUse(id: String, block: () -> Unit) =
        if (!givenItemExistsCurrently(id))
            block()
        else
            throw IllegalArgumentException("Id is already in use")

    private fun executeWhenIdIsInUse(id: String, block: () -> Unit) =
        if (givenItemExistsCurrently(id))
            block()
        else
            throw IllegalArgumentException("No item with given id found")


    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(id, intArrayOf(0, 0, 0))
            collidingItem?.apply { producer.send(session.createTextMessage(ReplaceEvent(this, id, intArrayOf(0, 0, 0), doCreateItem = true).serialize())) } ?: producer.send(session.createTextMessage(CreateEvent(
                MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)
            ).serialize()))
        }

    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(id, position)
            collidingItem?.apply { producer.send(session.createTextMessage(ReplaceEvent(this, id, position, value, doCreateItem = true).serialize())) } ?: producer.send(session.createTextMessage(CreateEvent(
                MovingItemImpl(id, position, 0, value)
            ).serialize()))
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            producer.send(session.createTextMessage(DeleteEvent(id).serialize()))
        }

    override fun moveItem(id: String, vector: IntArray) =
        executeWhenIdIsInUse(id) {
            val lastDeleteEvent = eventStore.getAllEvents().indexOfLast { event -> event is RemoveEvent && event.id == id}
            val countOfMoves =
                if (lastDeleteEvent == -1)
                    eventStore.getAllEvents().filterIsInstance<MoveEvent>().count { moveEvent -> moveEvent.id == id }
                else
                    eventStore.getAllEvents().slice(0..lastDeleteEvent).filterIsInstance<MoveEvent>().count { moveEvent -> moveEvent.id == id }

            if (countOfMoves < 20) {
                val collidingItem = findCollidingItem(id, vector)
                collidingItem?.apply { producer.send(session.createTextMessage(ReplaceEvent(this, id, vector, doCreateItem = false).serialize())) } ?: producer.send(session.createTextMessage(MoveEvent(id, vector).serialize()))
            }
            else deleteItem(id)
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            producer.send(session.createTextMessage(ChangeValueEvent(id, newValue).serialize()))
        }

    fun givenItemExistsCurrently(id: String): Boolean {
        val lastCreateEvent = eventStore.getAllEvents().indexOfLast { event -> event is CreateEvent && event.id == id}
        val lastDeleteEvent = eventStore.getAllEvents().indexOfLast { event -> event is RemoveEvent && event.id == id}
        return lastDeleteEvent < lastCreateEvent
    }

    private fun findItemPosition (id: String, existingItemEvents: List<Event>): Pair<String, IntArray> {
        val givenItemEvents = existingItemEvents.filter { it.id == id }
        val givenItemPositionDimension = (givenItemEvents.find { it is CreateEvent } as CreateEvent).item.location.size
        val givenItemPosition = givenItemEvents.fold(IntArray(givenItemPositionDimension)) { sum, element ->
            when (element) {
                is CreateEvent -> {
                    sum.addValues(element.item.location)
                }

                is MoveEvent -> {
                    sum.addValues(element.vector)
                }
                else -> {
                    sum
                }
            }
        }
        return Pair(id, givenItemPosition)
    }

    private fun findNewItemPosition (id: String, existingItemEvents: List<Event>, vector: IntArray): Pair<String, IntArray> {
        val givenItemEvents = existingItemEvents.filter { it.id == id }
        if (givenItemEvents.isNotEmpty()){
            val givenItemPositionDimension = (givenItemEvents.find { it is CreateEvent } as CreateEvent).item.location.size
            val givenItemPosition = givenItemEvents.fold(IntArray(givenItemPositionDimension)) { sum, element ->
                when (element) {
                    is CreateEvent -> {
                        sum.addValues(element.item.location)
                    }

                    is MoveEvent -> {
                        sum.addValues(element.vector)
                    }

                    else -> {
                        sum
                    }
                }.addValues(vector)
            }
            return Pair(id, givenItemPosition)
        }
        else {
            return Pair(id, vector)
        }
    }

    private fun findCollidingItem(id: String, vector: IntArray): String? {
        val currentItemsEvents =
            eventStore.getAllEvents()
                .groupBy { it.id }
                .filter { givenItemExistsCurrently(it.key) }
                .map { it.key to if (it.value.indexOfLast { event -> event is RemoveEvent } != -1) it.value.slice(0..it.value.indexOfLast { event -> event is RemoveEvent }) else it.value }
                .flatMap { it.second }

        val givenItemPosition = findNewItemPosition(id, currentItemsEvents, vector).second

        val otherPositions = currentItemsEvents.groupBy { it.id }
            .filter { it.key != id }
            .map { findItemPosition(it.key, it.value) }

        return otherPositions.find { it.second.contentEquals(givenItemPosition) }?.first
    }

}
