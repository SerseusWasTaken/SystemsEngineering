package esper

import kotlinx.datetime.Instant
import utils.Measurement
import java.security.Timestamp

data class AverageEvent(val sensor: Int, val averageSpeed: Double)
data class FlattenedMeasurement(val time: Long, val sensor: Int, val speed: Double)
data class SpeedDropEvent(val time: Instant, val sensor: Int, val speedDrop: Double)
