package command.events

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EventTest {

    @Test
    fun testSerialization() {
        val event = MoveEvent("Test1", intArrayOf(1,2,3))
        val string = event.serialize()
        val deserialzedEvent = string.deserializeToEvent() as MoveEvent
        deserialzedEvent.vector shouldBe event.vector
        deserialzedEvent.id shouldBe event.id
    }
}