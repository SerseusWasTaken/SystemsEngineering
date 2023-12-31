package kafka

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import utils.Measurement
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class Consumer(topics: List<String>, conf: Properties.() -> Unit) {
    val consumer: KafkaConsumer<String, String> = KafkaConsumer(
        Properties().apply(conf),
        org.apache.kafka.common.serialization.StringDeserializer(),
        org.apache.kafka.common.serialization.StringDeserializer()
    )


    init {
        consumer.subscribe(topics)
    }



    fun getData(): List<Measurement> = consumer.poll(1.seconds.toJavaDuration()).map { Measurement.getFromJSON(it.value()) }

    fun resetOffset() {
        consumer.seekToBeginning(consumer.assignment())
    }

}