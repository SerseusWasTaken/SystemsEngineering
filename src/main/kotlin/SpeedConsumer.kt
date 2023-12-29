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
    private val measurements = mutableMapOf<Int, List<Measurement>>()
    private var currentStartTime: Instant = Clock.System.now()
    private val currentTimeWindowValues = mutableMapOf<Int, List<Double>>()
    private val lastTimeWindowValues = mutableMapOf<Int, List<Double>>()
    fun getAndConsumeData(): List<Measurement> {
        val data = consumer.getData().map { Measurement.getFromJSON(it.value()) }.map { measurement ->
            Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) }.filter { it > 0 })
        }

        val dataGrouped = data.groupBy { it.sensor }
        dataGrouped.forEach {
            measurements[it.key] = if (measurements.containsKey(it.key)) measurements[it.key]?.plus(it.value)!!
            else it.value
        }
        return data
    }

    fun calculateAverageSpeedWithSlidingWindow(windowSize: Duration) {

        val dataWithStartingTime = measurements.map {
            it.key to it.value.filter { measurement -> measurement.isMeasurmentToBeUsed(currentStartTime, windowSize) }
        }
        //Update leftover measurements
        dataWithStartingTime.forEach { intListPair ->
            if (measurements[intListPair.first] != null) {
                val notYetUsedMeasurements = measurements[intListPair.first]!!.subtract(intListPair.second.toSet())
                measurements.replace(intListPair.first, notYetUsedMeasurements.toList())

            }
            if (currentTimeWindowValues[intListPair.first] != null) {
                currentTimeWindowValues[intListPair.first] =
                    intListPair.second.flatMap { it.values } + currentTimeWindowValues[intListPair.first]!!
            } else {
                currentTimeWindowValues[intListPair.first] = intListPair.second.flatMap { it.values }
            }

        }
        val res = currentTimeWindowValues.map { it.key to it.value.average().round(2) }.sortedBy { it.first }

        if ((currentStartTime + windowSize) < Clock.System.now()) {
            println("Neues Zeitfenster")
            //print final results of time window
            println("Ergebnis für das Zeitfenster $currentStartTime ist: $res")

            lastTimeWindowValues.clear()
            lastTimeWindowValues.putAll(currentTimeWindowValues)
            currentTimeWindowValues.clear()
            currentStartTime = Clock.System.now()
        }
        else
            println("Zwischenergebnis für Zeitfenster $currentStartTime: $res")
    }

    fun Measurement.isMeasurmentToBeUsed(startingTime: Instant, windowSize: Duration): Boolean {
        //What do we do with values in the past?
        //TODO:("Einbau past values")
        val isWithinTimeWindow = (this.time - startingTime) <= windowSize
        return isWithinTimeWindow
    }
}