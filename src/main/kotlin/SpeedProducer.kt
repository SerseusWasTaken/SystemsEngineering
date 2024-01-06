import kafka.Producer
import utils.Measurement
import utils.RandomDataGenerator
import utils.Utils.round

class SpeedProducer(val generator: RandomDataGenerator, val topic: String, val producer: Producer = Producer {
    setProperty("bootstrap.servers", "localhost:29092")
    setProperty("security.protocol", "PLAINTEXT")
}) {
    suspend fun produceData() {
        val measurement = generator.generateData(0, 0, 50)
        val newM = Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) })
        println(newM)
        producer.sendMessage(topic, measurement.sensor.toString(), measurement.serialize())
    }
}