package integration

import integration.di.TestModule
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Test
import query.api.MovingItemDTO
import query.impl.MovingItemDTOImpl
import java.util.Collections
import java.util.Enumeration

class IntegrationTest {
    private fun Enumeration<MovingItemDTO>.equalsEnum(param: Enumeration<MovingItemDTO>): Boolean {
        return this.asSequence().zip(param.asSequence()).all { (first, second) ->
            first.name == second.name && first.location.contentEquals(second.location) && first.value == second.value && first.moves == second.moves
        }
    }

    @Test
    fun `createItem should result in query to return that item`() {
        TestModule.handler.createItem("TestItem")
        TestModule.eventHandler.fetchEvent()
        assert(
            TestModule.queryModel.getMovingItems().equalsEnum(
                Collections.enumeration<MovingItemDTO>(
                    listOf(MovingItemDTOImpl("TestItem", intArrayOf(0, 0, 0), 0, 0))
                )
            )
        )
    }

    @Test
    fun `MoveItem should move item correctly`() {
        TestModule.handler.createItem("Item1", intArrayOf(1, 2, 3), 0)
        TestModule.eventHandler.fetchEvent()
        TestModule.handler.moveItem("Item1", intArrayOf(1, 2, 3))
        TestModule.eventHandler.fetchEvent()

        val newLocationOfItem = TestModule.queryModel.getMovingItems().nextElement().location
        newLocationOfItem shouldBe intArrayOf(2, 4, 6)
    }
}