package command.impl

import command.api.DomainModel
import command.api.MovingItemImpl
import command.events.*
import environment
import kafka.Consumer
import kafka.Producer
import org.apache.kafka.clients.consumer.ConsumerConfig
import query.utils.addValues
import java.util.Properties

class DomainModelImpl(
) : DomainModel {

    val producer: Producer
    val consumer: Consumer

    init {
        val producerProps = Properties()
        producerProps.setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        producerProps.setProperty("security.protocol", "PLAINTEXT")
        producer = Producer(producerProps)

        val consumerProps = Properties()
        consumerProps.setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        consumerProps.setProperty("group.id", "group2")
        consumerProps.setProperty("security.protocol", "PLAINTEXT")
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = Consumer(consumerProps, listOf("allEvents"))
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
    
    private fun sendMessage(message: String) {
        producer.sendMessage("allEvents", "default", message)
    }

    private fun getAllEvents(): List<Event> {
        consumer.resetOffset()
        val events = consumer.getEvents()
        println("All events: $events")
        return events
    }


    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(id, intArrayOf(0, 0, 0))
            collidingItem?.apply { sendMessage(ReplaceEvent(this, id, intArrayOf(0, 0, 0), doCreateItem = true).serialize()) } ?: sendMessage(CreateEvent(MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)).serialize())
        }

    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(id, position)
            collidingItem?.apply { sendMessage(ReplaceEvent(this, id, position, value, doCreateItem = true).serialize()) } ?: sendMessage(CreateEvent(
                MovingItemImpl(id, position, 0, value)
            ).serialize())
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            sendMessage(DeleteEvent(id).serialize())
        }

    override fun moveItem(id: String, vector: IntArray) =
        executeWhenIdIsInUse(id) {
            val lastDeleteEvent = getAllEvents().indexOfLast { event -> event is RemoveEvent && event.id == id}
            val countOfMoves =
                if (lastDeleteEvent == -1)
                    getAllEvents().filterIsInstance<MoveEvent>().count { moveEvent -> moveEvent.id == id }
                else
                    getAllEvents().slice(0..lastDeleteEvent).filterIsInstance<MoveEvent>().count { moveEvent -> moveEvent.id == id }

            if (countOfMoves < 20) {
                val collidingItem = findCollidingItem(id, vector)
                collidingItem?.apply { sendMessage(ReplaceEvent(this, id, vector, doCreateItem = false).serialize()) } ?: sendMessage(MoveEvent(id, vector).serialize())
            }
            else deleteItem(id)
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            sendMessage(ChangeValueEvent(id, newValue).serialize())
        }

    fun givenItemExistsCurrently(id: String): Boolean {
        val lastCreateEvent = getAllEvents().indexOfLast { event -> event is CreateEvent && event.id == id}
        val lastDeleteEvent = getAllEvents().indexOfLast { event -> event is RemoveEvent && event.id == id}
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
            getAllEvents()
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
