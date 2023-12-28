import kafka.Consumer
import kotlinx.datetime.Instant
import org.apache.kafka.clients.consumer.ConsumerConfig
import utils.Measurement
import utils.Utils.round
import kotlin.time.Duration
import kotlinx.datetime.Clock

class SpeedConsumer(
    val consumer: Consumer = Consumer(listOf("data")) {
        setProperty("bootstrap.servers", "localhost:29092")
        setProperty("security.protocol", "PLAINTEXT")
        setProperty("group.id", "group1")
        setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "SpeedConsumer1")
        // setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
) {

    //TODO:("Caching von Werten")
    //TODO:("Richtige Durchschnittsberechung")
    val values = mutableMapOf<Int, List<Measurement>>()
    var currentStartTime: Instant = Clock.System.now()
    fun getAndConsumeData(): List<Measurement> {
        val data = consumer.getData().map { Measurement.getFromJSON(it.value()) }.map { measurement ->
                Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) })
            }

        val dataGrouped = data.groupBy { it.sensor }
        dataGrouped.forEach {
            values[it.key] = if (values.containsKey(it.key)) values[it.key]?.plus(it.value)!!
            else it.value
        }
        return data
    }

    fun calculateAverageSpeedWithSlidingWindow(windowSize: Duration) {

        val dataWithStartingTime = values.map {
                it.key to it.value.filter { measurement ->  measurement.isMeasurmentToBeUsed(currentStartTime, windowSize)}
        }
        //Update values
        dataWithStartingTime.forEach {
            if(values[it.first] != null) {
                val notYetUsedMeasurements = values[it.first]!!.subtract(it.second.toSet())
                values.replace(it.first, notYetUsedMeasurements.toList())
            }
        }
        val res = dataWithStartingTime.map { it.first to it.second.flatMap { it.values }.average() }
        println("Hier: ${res.sortedBy { it.first }}")


        if ((currentStartTime + windowSize) < Clock.System.now()) {
            currentStartTime = Clock.System.now()
            println("Neues Zeitfenster")
        }

    }

    fun Measurement.isMeasurmentToBeUsed(startingTime: Instant, windowSize: Duration): Boolean {
        //What do we do with values in the past?
        //TODO:("Einbau past values")
        val isWithinTimeWindow = (this.time - startingTime) <= windowSize
        println(isWithinTimeWindow)
        return isWithinTimeWindow
    }
}