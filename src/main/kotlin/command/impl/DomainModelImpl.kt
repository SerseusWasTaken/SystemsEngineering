package command.impl

import command.api.DomainModel
import command.api.EventStore
import command.api.MovingItem
import command.api.MovingItemImpl
import command.events.*
import environment
import jakarta.jms.TextMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import query.utils.addValues
import java.util.Properties

class DomainModelImpl(
    val eventStore: EventStore,
    private val itemStore: MutableMap<String, MovingItem> = mutableMapOf()
) : DomainModel {
    val props = Properties()
    val producer: KafkaProducer<String, String>


    init {
        props.setProperty("bootstrap.servers", "localhost:${environment.brokers.first().port}")
        props.setProperty("security.protocol", "PLAINTEXT")
        producer = KafkaProducer(
            props,
            StringSerializer(),
            StringSerializer()
        )
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

    private fun sendMessage(key: String, event: String) {
        val msg = ProducerRecord("allEvents", key, event)
        producer.send(msg)
    }


    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(intArrayOf(0, 0, 0))
            collidingItem?.apply { sendMessage("test", ReplaceEvent(this, id, intArrayOf(0, 0, 0), doCreateItem = true).serialize()) } ?: sendMessage("test", CreateEvent(
                MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)
            ).serialize())
            itemStore[id] = MovingItemImpl(id, intArrayOf(0,0,0), 0, 0)
        }

    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(position)
            collidingItem?.apply { sendMessage("test", ReplaceEvent(this, id, position, value, doCreateItem = true).serialize()) } ?: sendMessage("test", CreateEvent(
                MovingItemImpl(id, position, 0, value)
            ).serialize())
            itemStore[id] = MovingItemImpl(id, position, 0, value)
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            sendMessage("test", DeleteEvent(id).serialize())
            itemStore.remove(id)
        }

    override fun moveItem(id: String, vector: IntArray) =
        executeWhenIdIsInUse(id) {

            if (itemStore[id]!!.moves < 20) {
                val newPosition = itemStore[id]!!.location.addValues(vector)
                val collidingItem = findCollidingItem(newPosition)
                collidingItem?.apply {
                    sendMessage(
                        "test",
                            ReplaceEvent(
                                this,
                                id,
                                newPosition,
                                doCreateItem = false
                            ).serialize()
                        )
                } ?: sendMessage("test", MoveEvent(id, newPosition).serialize())
                val oldItem = itemStore[id]!!
                itemStore.replace(id, MovingItemImpl(id, newPosition, oldItem.moves + 1, oldItem.value))
            } else deleteItem(id)
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            sendMessage("test", ChangeValueEvent(id, newValue).serialize())
        }

    fun givenItemExistsCurrently(id: String): Boolean {
        return itemStore.containsKey(id)
    }

    private fun findCollidingItem(vector: IntArray): String? {
        return itemStore.values.find { it.location.contentEquals(vector) }?.name
    }

}
