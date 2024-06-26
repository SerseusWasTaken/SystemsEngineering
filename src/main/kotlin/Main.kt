import com.espertech.esper.common.client.configuration.Configuration
import com.espertech.esper.compiler.client.CompilerArguments
import com.espertech.esper.compiler.client.EPCompilerProvider
import com.espertech.esper.runtime.client.EPRuntimeProvider
import esper.AverageSpeed
import esper.FlattenedMeasurement
import esper.SpeedDropEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

import utils.Measurement
import utils.RandomDataGenerator
import utils.Utils.round

fun main(args: Array<String>) {

    val speedHistory = emptyMap<Int, List<Double>>().toMutableMap()

    val config = Configuration()
    config.common.addEventType(Measurement::class.java)
    config.common.addEventType(FlattenedMeasurement::class.java)
    config.common.addEventType(SpeedDropEvent::class.java)
    config.common.addEventType(AverageSpeed::class.java)
    val compiler = EPCompilerProvider.getCompiler()
    val compilerArgs = CompilerArguments(config)
    val q1 = "@name('getMeasurements') select time, sensor, doubleList from Measurement;\n"
    val q2 = "@name('getAverage') select averageSpeed, sensor from AverageSpeed;\n"
    val q4 = "insert into AverageSpeed select avg(speed) as averageSpeed, sensor, current_timestamp from FlattenedMeasurement#ext_timed_batch(time, 10 sec) group by sensor having count(speed) > 0;\n"
    val q5 = "@name('speedDropEvent')select * from pattern [every avgSpeed1=AverageSpeed -> avgSpeed2=AverageSpeed(sensor=avgSpeed1.sensor and averageSpeed < avgSpeed1.averageSpeed - 15) where timer:within(30 seconds)];\n"
    val q6 = "@name('getAverageForStreet') select avg(speed) as averageSpeed, sensor from FlattenedMeasurement#ext_timed_batch(time, 10 sec) where sensor IN (1,2,3) group by sensor having count(speed) > 0;\n"
    val epCompiled = compiler.compile(q1 + q2 + q4 + q5 + q6, compilerArgs)

    val runtime = EPRuntimeProvider.getDefaultRuntime(config)
    runtime.initialize()
    val deployment = runtime.deploymentService.deploy(epCompiled)

    val getMeasurementsStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getMeasurements")
    getMeasurementsStatement.addListener { newData, oldData, statement, runtime ->
        val time: Instant = newData[0].get("time") as Instant
        val sensor = newData[0].get("sensor") as Int
        val values = newData[0].get("doubleList") as List<Double>
        val m = Measurement(time, sensor, values)
        val flattened = m.doubleList
            .filter{d -> d >= 0}
            .map { FlattenedMeasurement(m.time.toEpochMilliseconds(), m.sensor, (it * 3.6).round(2)) }
        flattened.forEach { runtime.eventService.sendEventBean(it, "FlattenedMeasurement") }
    }

    val getAverageStatment = runtime.deploymentService.getStatement(deployment.deploymentId, "getAverage")
    getAverageStatment.addListener { newData, oldData, statement, runtime ->
        newData.forEach {
            val avg = it.get("averageSpeed") as Double? ?: Double.NaN
            val sensor = it.get("sensor") as Int
            addSpeedValueToMap(speedHistory, sensor, avg)
            println("Speed over time for sensor $sensor: ${speedHistory[sensor]}")
        }
    }

    val getAverageForStreetStatment2 = runtime.deploymentService.getStatement(deployment.deploymentId, "getAverageForStreet")
    getAverageForStreetStatment2.addListener { newData, oldData, statement, runtime ->
        val res = newData.map {
            val avg = it.get("averageSpeed") as Double? ?: Double.NaN
            val sensor = it.get("sensor") as Int
            sensor to avg
        }
        println("Durchschnittsgeschwindigkeiten auf Streckenabschnitt (1,2,3): ${res}")
    }

    val newAverage = runtime.deploymentService.getStatement(deployment.deploymentId, "speedDropEvent")
    newAverage.addListener { eventBeans, eventBeans2, epStatement, epRuntime ->
        val speed = eventBeans[0].get("avgSpeed1") as AverageSpeed
        val speed2 = eventBeans[0].get("avgSpeed2") as AverageSpeed
        val diff = speed.averageSpeed - speed2.averageSpeed
        println("ACHTUNG: Es gab im Streckenabschnitt von Sensor ${speed.sensor} einen Geschwindigkeitsabfall um ${diff}km/h. Hier könnte sich ein Stau Bilden")
    }

    //Aktuell ohne Kafka
    //KafkaInitilizer.createTopic("data", 4)
    val producer = SpeedProducer(RandomDataGenerator(5, 1000, 1000, 0.0), runtime)
    runBlocking {
        launch(Dispatchers.IO) {
            while (true) producer.produceData()
        }
    }


}

fun addSpeedValueToMap(map: MutableMap<Int, List<Double>>, sensorId: Int, speed: Double) {
    if (map.containsKey(sensorId)) {
        val oldSpeeds = map[sensorId]!!
        val newList = oldSpeeds + speed
        map[sensorId] = newList
    }
    else {
        map[sensorId] = listOf(speed)
    }
}


