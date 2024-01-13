package esper

import kotlinx.datetime.Instant
import java.security.Timestamp

data class AverageEvent(val sensor: Int, val averageSpeed: Double)
data class FlattenedMeasurement(val time: Long, val sensor: Int, val speed: Double)