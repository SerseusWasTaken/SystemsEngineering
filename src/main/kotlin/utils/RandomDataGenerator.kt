package utils

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

class RandomDataGenerator(
    val numberOfSensors: Int, val m1: Int, val m2: Int, val likelihoodOfGettingNoMeasurement: Double
) {
    fun createRandomData(id: Int, numberOfSpeedValues: Int, vMin: Int, vMax: Int): Measurement {
        val timestamp = Clock.System.now()
        val data = (0..numberOfSpeedValues).map {
            val randomSpeed = Utils.getRandomNumber(vMin, vMax, 1)
            // 1% chance of getting random negative value
            if (Utils.beTrueForChanceOf(1.0)) randomSpeed * -1
            else randomSpeed
        }.filter { Utils.beTrueForChanceOf(100.0 - likelihoodOfGettingNoMeasurement) }
        return Measurement(timestamp, id, data)
    }

    suspend fun generateData( numberOfSpeedValues: Int, vMin: Int, vMax: Int): Measurement {
        val timeToWait = Utils.getRandomNumber(m1, m2, 0).milliseconds
        val sensor = Utils.getRandomNumber(1, numberOfSensors, 0).toInt()

        delay(timeToWait)
        return createRandomData(sensor, numberOfSpeedValues, vMin, vMax)
    }
}