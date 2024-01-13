import com.espertech.esper.runtime.client.EPRuntime
import kafka.Producer
import kotlinx.datetime.Clock
import utils.Measurement
import utils.RandomDataGenerator
import utils.Utils.round

class SpeedProducer(val generator: RandomDataGenerator, val runtime: EPRuntime) {
    suspend fun produceData() {
        val measurement = generator.generateData(0, 0, 50)
        val newM = Measurement(measurement.time, measurement.sensor, measurement.doubleList.map { (it * 3.6).round(2) })
        println("Sending: $newM")
        runtime.eventService.sendEventBean(measurement, "Measurement")
    }
}