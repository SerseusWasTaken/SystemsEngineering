package kafka

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
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



    fun getData(): ConsumerRecords<String, String> = consumer.poll(1.seconds.toJavaDuration())

    fun resetOffset() {
        consumer.seekToBeginning(consumer.assignment())
    }

}