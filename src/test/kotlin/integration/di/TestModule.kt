package integration.di

import SpeedConsumer
import SpeedProducer
import io.mockk.mockk
import io.mockk.spyk
import kafka.Consumer
import kafka.Producer
import utils.RandomDataGenerator


object TestModule {

    val mockConsumer = mockk<Consumer>(relaxed= true)

    val mockProducer = mockk<Producer>(relaxed= true)

    val randomDataGenerator = spyk(RandomDataGenerator(1, 1, 1000, 0.0))

    val speedConsumer = SpeedConsumer(mockConsumer)

    val speedProducer = SpeedProducer(randomDataGenerator, "testTopic", mockProducer)

}
