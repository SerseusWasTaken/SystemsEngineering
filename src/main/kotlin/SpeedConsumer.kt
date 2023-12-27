import kafka.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import utils.Measurement
import utils.Utils.round
import kotlin.time.Duration

class SpeedConsumer(
    val consumer: Consumer = Consumer(listOf("data")) {
        setProperty("bootstrap.servers", "localhost:29092")
        setProperty("security.protocol", "PLAINTEXT")
        setProperty("group.id", "group1")
        setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "SpeedConsumer1")
        // setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
) {

    var values = mutableMapOf<Int, List<Measurement>>()
    fun getAndConsumeData(): List<Measurement> {
        val data = consumer.getData().map { Measurement.getFromJSON(it.value()) }.map { measurement ->
                Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) })
            }
        return data
    }

    fun calculateAverageSpeedWithSlidingWindow(data: List<Measurement>, windowSize: Duration) {
        val dataGrouped = data.groupBy { it.sensor }
        dataGrouped.forEach {
            values[it.key] = if (values.containsKey(it.key)) values[it.key]?.plus(it.value)!!
            else it.value
        }
        val dataWithStartingTime = values.map {
            Triple(it.value.first().time,
                it.key,
                it.value.filter { measurement -> !((measurement.time - it.value.first().time).isNegative()) })
        }
        val r1 = dataWithStartingTime.map {
            Triple(it.first,
                it.second,
                it.third.filter { measurement -> measurement.time.minus(it.first) <= windowSize })
        }
        val res = r1.map { it.second to it.third.flatMap { it.values }.average().round(3) }
        println("Hier: ${res.sortedBy { it.first }}")
    }
}