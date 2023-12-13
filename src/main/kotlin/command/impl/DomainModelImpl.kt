package command.impl

import command.api.DomainModel
import command.api.EventStore
import command.api.MovingItem
import command.api.MovingItemImpl
import command.events.*
import jakarta.jms.MessageProducer
import jakarta.jms.Queue
import jakarta.jms.Session
import jakarta.jms.TextMessage
import org.apache.activemq.ActiveMQConnectionFactory
import query.utils.addValues

class DomainModelImpl(
    val eventStore: EventStore,
    private val itemStore: MutableMap<String, MovingItem> = mutableMapOf()
) : DomainModel {
    // This stuff probably has to get moved to the command handler and is tightly coupled here
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

    private fun sendMessage(msg: TextMessage) {
        msg.setLongProperty("timestamp", System.currentTimeMillis())
        producer.send(msg)
    }


    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(intArrayOf(0, 0, 0))
            collidingItem?.apply { sendMessage(session.createTextMessage(ReplaceEvent(this, id, intArrayOf(0, 0, 0), doCreateItem = true).serialize())) } ?: sendMessage(session.createTextMessage(CreateEvent(
                MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)
            ).serialize()))
            itemStore[id] = MovingItemImpl(id, intArrayOf(0,0,0), 0, 0)
        }

    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            val collidingItem = findCollidingItem(position)
            collidingItem?.apply { sendMessage(session.createTextMessage(ReplaceEvent(this, id, position, value, doCreateItem = true).serialize())) } ?: sendMessage(session.createTextMessage(CreateEvent(
                MovingItemImpl(id, position, 0, value)
            ).serialize()))
            itemStore[id] = MovingItemImpl(id, position, 0, value)
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            sendMessage(session.createTextMessage(DeleteEvent(id).serialize()))
            itemStore.remove(id)
        }

    override fun moveItem(id: String, vector: IntArray) =
        executeWhenIdIsInUse(id) {

            if (itemStore[id]!!.moves < 20) {
                val newPosition = itemStore[id]!!.location.addValues(vector)
                val collidingItem = findCollidingItem(newPosition)
                collidingItem?.apply {
                    sendMessage(
                        session.createTextMessage(
                            ReplaceEvent(
                                this,
                                id,
                                newPosition,
                                doCreateItem = false
                            ).serialize()
                        )
                    )
                } ?: sendMessage(session.createTextMessage(MoveEvent(id, newPosition).serialize()))
                val oldItem = itemStore[id]!!
                itemStore.replace(id, MovingItemImpl(id, newPosition, oldItem.moves + 1, oldItem.value))
            } else deleteItem(id)
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            sendMessage(session.createTextMessage(ChangeValueEvent(id, newValue).serialize()))
        }

    fun givenItemExistsCurrently(id: String): Boolean {
        return itemStore.containsKey(id)
    }

    private fun findCollidingItem(vector: IntArray): String? {
        return itemStore.values.find { it.location.contentEquals(vector) }?.name
    }

}
