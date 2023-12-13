package kafka

import command.events.deserializeToEvent
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class Consumer(props: Properties, topics: List<String>) {
    val consumer: KafkaConsumer<String, String> = KafkaConsumer(
        props,
        org.apache.kafka.common.serialization.StringDeserializer(),
        org.apache.kafka.common.serialization.StringDeserializer()
    )


    init {
        consumer.subscribe(topics)
    }



    fun getEvents() = consumer.poll(1.seconds.toJavaDuration()).map { it.value().deserializeToEvent() }

    fun resetOffset() {
        consumer.seekToBeginning(consumer.assignment())
    }

}