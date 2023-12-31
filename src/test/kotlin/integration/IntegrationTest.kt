package integration

import integration.di.TestModule
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.unmockkAll
import io.mockk.verify
import kafka.Consumer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
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
        every { TestModule.mockConsumer.getData() } returns listOf(Measurement(Clock.System.now(), 1, listOf(1.0)))
        TestModule.speedConsumer.getAndConsumeData()
        TestModule.speedConsumer.calculateAverageSpeedWithSlidingWindow(10.milliseconds)
        delay(10.milliseconds)
        every { TestModule.mockConsumer.getData() } returns listOf(Measurement(Clock.System.now() + 1000.milliseconds, 1, listOf(1.0)))
        TestModule.speedConsumer.getAndConsumeData()
        TestModule.speedConsumer.calculateAverageSpeedWithSlidingWindow(10.milliseconds)

        TestModule.speedConsumer.lastTimeWindowValues.size shouldBe 1

    }
}
