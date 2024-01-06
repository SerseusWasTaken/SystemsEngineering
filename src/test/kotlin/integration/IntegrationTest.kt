package integration

import integration.di.TestModule
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.unmockkAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.AfterEach
import utils.Measurement
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds


class IntegrationTest {

    @AfterEach
    fun teardown() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun `values from the future should not fall into current time window`(): Unit = runBlocking {


    }
}
