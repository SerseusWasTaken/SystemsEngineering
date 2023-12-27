package utils

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Measurement(val time: Instant, val sensor: Int, val values: List<Double>) {
    override fun toString(): String {
        var baseString = "$time $sensor"
        values.forEach { baseString += " $it," }
        return baseString.trim(',')
    }

    fun serialize(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun getFromJSON(jsonString: String): Measurement {
            return Json.decodeFromString(jsonString)
        }
    }
}