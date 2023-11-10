package command.impl

import command.events.CreateEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DomainModelImplTest {

    val mockEventStore = mockk<EventStoreImpl>(relaxed = true)
    val store: MutableSet<String> = mockk<MutableSet<String>>(relaxed = true)
    lateinit var cut: DomainModelImpl
    @BeforeEach
    fun setUp() {
        cut = DomainModelImpl(mockEventStore, store)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createItem should fail if id is already in use`() {
        every { store.contains(any()) } returns true
        assertThrows(IllegalArgumentException::class.java) {
            cut.createItem("")
        }
    }

    @Test
    fun `createItem should create an event`() {
        every { store.contains(any()) } returns false
        cut.createItem("ItemTest")
        verify {
            mockEventStore.storeEvent(any())
        }
    }
}