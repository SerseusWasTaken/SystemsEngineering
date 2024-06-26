package kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class Producer(conf: Properties.() -> Unit) {
    val producer = KafkaProducer(
        Properties().apply(conf),
        StringSerializer(),
        StringSerializer()
    )

    fun sendMessage(topic: String, key: String = "default", msg: String) {
        producer.send(ProducerRecord(topic, key, msg))
    }
}
