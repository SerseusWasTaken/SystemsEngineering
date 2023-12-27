import kafka.Producer
import utils.RandomDataGenerator

class SpeedProducer(val generator: RandomDataGenerator, val topic: String, val producer: Producer = Producer {
    setProperty("bootstrap.servers", "localhost:29092")
    setProperty("security.protocol", "PLAINTEXT")
}) {
    suspend fun produceData() {
        val measurement = generator.generateData(5, 0, 50)
        producer.sendMessage(topic, msg = measurement.serialize())
    }
}