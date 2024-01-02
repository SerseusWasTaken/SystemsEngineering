import kafka.Consumer
import kotlinx.datetime.Instant
import org.apache.kafka.clients.consumer.ConsumerConfig
import utils.Measurement
import utils.Utils.round
import kotlin.time.Duration
import kotlinx.datetime.Clock
import org.jetbrains.annotations.VisibleForTesting

class SpeedConsumer(
    val windowSize: Duration,
    val consumer: Consumer = Consumer(listOf("data")) {
        setProperty("bootstrap.servers", "localhost:29092")
        setProperty("security.protocol", "PLAINTEXT")
        setProperty("group.id", "group1")
        setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "SpeedConsumer1")
        // setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
) {
    @VisibleForTesting
    val measurements = mutableMapOf<Int, List<Measurement>>()
    private var currentStartTime: Instant = Clock.System.now()
    val currentTimeWindowValues = mutableMapOf<Int, List<Double>>()
    val averageOfPast = mutableListOf<Pair<Map<Int, List<Double>>, Instant>>()

    init {
        averageOfPast.clear()
    }
    fun getAndConsumeData(): List<Measurement> {
        val data = consumer.getData().map { measurement ->
            Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) }.filter { it > 0 })
        }

        val dataGrouped = data.groupBy { it.sensor }
        dataGrouped.forEach {
            measurements[it.key] = if (measurements.containsKey(it.key)) measurements[it.key]?.plus(it.value)!!
            else it.value
        }
        return data
    }

    fun calculateAverageSpeedWithinTimeWindow() {
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

            averageOfPast.add(emptyMap<Int, MutableList<Double>>().toMutableMap().apply {
                res.forEach {
                    if (this.containsKey(it.first))
                        this[it.first]?.add(it.second)
                    else
                        this[it.first] = mutableListOf(it.second)
                }
            }.toMap() to currentStartTime)
            currentTimeWindowValues.clear()
            currentStartTime = Clock.System.now()
        }
        else
            println("Zwischenergebnis für Zeitfenster $currentStartTime: $res")
    }

    //Aufgabenteil 1
    fun getAverageSpeedOverTimeBySensor(sensor: Int): List<Double> {
        return averageOfPast.map { it.first.filter { it.key == sensor }.flatMap { it.value } }.flatten()
    }

    //Aufgabenteil 2
    fun getAverageOfRoad(time: Instant, vararg sensors: Int): Map<Int, List<Double>> {
        val filteredForTime = averageOfPast.find { it.second.isOtherWithinTimeWindow(time) }?.first
        if (filteredForTime == null)
            error("Given time is not within any timeWindow!")
        val filteredForSensors = filteredForTime.filter { sensors.contains(it.key) }
        return filteredForSensors
    }

    fun Instant.isOtherWithinTimeWindow(other: Instant): Boolean {
        val isInPast = this > other
        val isAfterTimeWindow = (this + this@SpeedConsumer.windowSize) < other
        return !isInPast && !isAfterTimeWindow
    }

    fun Measurement.isMeasurmentToBeUsed(startingTime: Instant, windowSize: Duration): Boolean {
        //What do we do with values in the past?
        //TODO:("Einbau past values")
        val isWithinTimeWindow = (this.time - startingTime) <= windowSize
        return isWithinTimeWindow
    }
}