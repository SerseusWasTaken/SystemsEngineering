package kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class Producer(props: Properties) {
    val producer = KafkaProducer(
        props,
        StringSerializer(),
        StringSerializer()
    )

    fun sendMessage(topic: String, key: String = "default", msg: String) {
        producer.send(ProducerRecord(topic, key, msg))
    }
}
