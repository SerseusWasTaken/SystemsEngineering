package integration

import integration.di.TestModule
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import query.api.MovingItemDTO
import query.impl.MovingItemDTOImpl
import java.util.Collections
import java.util.Enumeration

class IntegrationTest {

    @AfterEach
    fun teardown() {
        TestModule.queryDatabase.data.clear()
        TestModule.eventStore.queue.clear()
        TestModule.eventStore.allEvents.clear()
    }

    @Test
    fun `createItem should result in query to return that item`() {
        TestModule.handler.createItem("TestItem")
        TestModule.eventHandler.fetchEvent()
        TestModule.queryModel.getMovingItems() shouldBe enumerationOf(MovingItemDTOImpl("TestItem", intArrayOf(0, 0, 0), 0, 0))
    }

    @Test
    fun `CreateItem should create item correctly when it collides with existing item`() {
        TestModule.handler.createItem("Item1", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()
        TestModule.handler.createItem("Item2", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()

        val items = TestModule.queryModel.getMovingItems().toList()
        items.size shouldBe 1
    }

    @Test
    fun `MoveItem should move item correctly`() {
        TestModule.handler.createItem("Item3", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()
        TestModule.handler.moveItem("Item3", intArrayOf(1, 2, 3))
        TestModule.eventHandler.fetchEvent()

        val newLocationOfItem = TestModule.queryModel.getMovingItems().nextElement().location
        newLocationOfItem shouldBe intArrayOf(2, 4, 6)
    }

    @Test
    fun `MoveItem should move item correctly when it collides moves`() {
        TestModule.handler.createItem("Item5", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()
        TestModule.handler.createItem("Item6", intArrayOf(0, 0, 0), 0)
        TestModule.eventHandler.fetchEvent()

        TestModule.handler.moveItem("Item6", intArrayOf(1, 2, 3))
        TestModule.eventHandler.fetchEvent()
        val items = TestModule.queryModel.getMovingItems().toList()
        items.size shouldBe 1
    }

    @Test
    fun `DeleteItem should delete Item from QueryDatabse`() {
        TestModule.handler.createItem("Item7", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()
        TestModule.queryModel.getMovingItems().toList().size shouldBe 1

        TestModule.handler.deleteItem("Item7")
        TestModule.eventHandler.fetchEvent()

        verify { TestModule.queryDatabase.deleteItem("Item7") }
        TestModule.queryModel.getMovingItems() shouldBe enumerationOf()
    }

    companion object {
        private fun Enumeration<MovingItemDTO>.equalsEnum(param: Enumeration<MovingItemDTO>): Boolean {
            if (this.toList().size != param.toList().size)
                return false
            return this.asSequence().zip(param.asSequence()).all { (first, second) ->
                first.name == second.name && first.location.contentEquals(second.location) && first.value == second.value && first.moves == second.moves
            }
        }

        private infix fun <T : Enumeration<MovingItemDTO>, U : T> T.shouldBe(expected: U): Unit =
            assert(this.equalsEnum(expected))

        private fun enumerationOf(vararg param: MovingItemDTO): Enumeration<MovingItemDTO> = Collections.enumeration<MovingItemDTO>(param.asList())
    }
}