package command.impl

import command.api.DomainModel
import command.api.EventStore
import command.events.ChangeValueEvent
import command.events.CreateEvent
import command.events.DeleteEvent
import command.events.MoveEvent

class DomainModelImpl(
    private val eventStore: EventStore,
    //Visible for testing
    val data: MutableSet<String> = mutableSetOf()
) : DomainModel {

    private fun executeWhenIdIsNotInUse(id: String, block: () -> Unit) =
        if (!data.contains(id))
            block()
        else
            throw IllegalArgumentException("Id is already in use")

    private fun executeWhenIdIsInUse(id: String, block: () -> Unit) =
        if (data.contains(id))
            block()
        else
            throw IllegalArgumentException("No item with given id found")

    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            eventStore.storeEvent(CreateEvent(MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)))
            data.add(id)
        }

    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            eventStore.storeEvent(CreateEvent(MovingItemImpl(id, position, 0, value)))
            data.add(id)
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            eventStore.storeEvent(DeleteEvent(id))
            data.remove(id)
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
                collidingItem?.apply { eventStore.storeEvent(ReplaceEvent(this, id, vector)) } ?: eventStore.storeEvent(MoveEvent(id, vector))
            }
            else deleteItem(id)
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            eventStore.storeEvent(ChangeValueEvent(id, newValue))
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