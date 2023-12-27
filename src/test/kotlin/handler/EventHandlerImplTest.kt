package handler

import command.events.ChangeValueEvent
import command.events.CreateEvent
import command.events.DeleteEvent
import command.events.MoveEvent
import command.impl.EventStoreImpl
import command.api.MovingItemImpl
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kafka.Consumer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import query.impl.QueryDatabaseImpl

class EventHandlerImplTest {

    private lateinit var cut: EventHandler
    private val mockConsumer = mockk<Consumer>(relaxed = true)
    private val mockQueryDatabse = mockk<QueryDatabaseImpl>(relaxed = true)

    @BeforeEach
    fun setup() {
        cut = EventHandlerImpl(mockQueryDatabse, mockConsumer)
    }
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `EventHandler should execute the right function depending on event type`() {
        val createEvent = CreateEvent(MovingItemImpl("Item1", intArrayOf(), 0, 0))
        cut.handleEvent(createEvent)
        verify { mockQueryDatabse.createItem(any()) }

        val changeValueEvent = ChangeValueEvent("Item2", 1)
        cut.handleEvent(changeValueEvent)
        verify { mockQueryDatabse.changeValue(changeValueEvent.id, changeValueEvent.newValue) }

        val deleteEvent = DeleteEvent("Item3")
        cut.handleEvent(deleteEvent)
        verify { mockQueryDatabse.deleteItem("Item3") }

        val moveEvent = MoveEvent("Item4", intArrayOf(1, 2, 3))
        cut.handleEvent(moveEvent)
        verify { mockQueryDatabse.moveLocation("Item4", intArrayOf(1, 2, 3)) }
    }
}