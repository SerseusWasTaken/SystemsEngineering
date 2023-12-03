package command.events

import command.api.MovingItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
sealed interface Event { val id: String}

@Serializable
sealed interface RemoveEvent: Event

@Serializable
data class CreateEvent(val item: MovingItem, override val id: String = item.name): Event

@Serializable
data class ChangeValueEvent(override val id: String, val newValue: Int): Event

@Serializable
data class DeleteEvent(override val id: String): RemoveEvent

@Serializable
data class MoveEvent(override val id: String, val vector: IntArray): Event

@Serializable
data class ReplaceEvent(override val id: String, val itemToMove: String, val vector: IntArray, val value: Int = 0, val doCreateItem: Boolean): RemoveEvent

fun Event.serialize(): String {
    return Json.encodeToString(this)
}

fun String.deserializeToEvent(): Event {
    return Json.decodeFromString<Event>(this)
}

