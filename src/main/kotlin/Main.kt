import com.espertech.esper.common.client.configuration.Configuration
import com.espertech.esper.compiler.client.CompilerArguments
import com.espertech.esper.compiler.client.EPCompilerProvider
import com.espertech.esper.runtime.client.EPRuntimeProvider
import esper.FlattenedMeasurement
import esper.SpeedDropEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

import utils.Measurement
import utils.RandomDataGenerator

fun main(args: Array<String>) {

    val averages = emptyList<Pair<Int, Double>>().toMutableList()
    val sensorsToGetAverageFor = listOf(1,2,3)

    val config = Configuration()
    config.common.addEventType(Measurement::class.java)
    config.common.addEventType(FlattenedMeasurement::class.java)
    config.common.addEventType(SpeedDropEvent::class.java)
    val compiler = EPCompilerProvider.getCompiler()
    val compilerArgs = CompilerArguments(config)
    val q1 = "@name('getMeasurements') select time, sensor, doubleList from Measurement;\n"
    val q2 = "@name('getFlattenedMeasurements') select * from FlattenedMeasurement;\n"
    val q3 = "@name('getAverage') select irstream avg(speed) as res, sensor from FlattenedMeasurement#ext_timed_batch(time, 10 sec) group by sensor;\n"
    val q4 = "@name('getSpeedDrop') select time, sensor, speedDrop from SpeedDropEvent;\n"
    val epCompiled = compiler.compile(q1 + q2 + q3 + q4, compilerArgs)

    val runtime = EPRuntimeProvider.getDefaultRuntime(config)
    runtime.initialize()
    val deployment = runtime.deploymentService.deploy(epCompiled)

    val getMeasurementsStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getMeasurements")
    getMeasurementsStatement.addListener { newData, oldData, statement, runtime ->
        val time: Instant = newData[0].get("time") as Instant
        val sensor = newData[0].get("sensor") as Int
        val values = newData[0].get("doubleList") as List<Double>
        val m = Measurement(time, sensor, values)
        val flattened = m.doubleList.filter{d -> d >= 0}.map { FlattenedMeasurement(m.time.toEpochMilliseconds(), m.sensor, it) }
        flattened.forEach { runtime.eventService.sendEventBean(it, "FlattenedMeasurement") }
    }

    val getFlattenedStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getFlattenedMeasurements")
    getFlattenedStatement.addListener { newData, oldData, statement, runtime ->
        val time = newData[0].get("time") as Long
        val sensor = newData[0].get("sensor") as Int
        val values = newData[0].get("speed") as Double
        val m = FlattenedMeasurement(time, sensor, values)
    }

    val getAverageStatment = runtime.deploymentService.getStatement(deployment.deploymentId, "getAverage")
    getAverageStatment.addListener { newData, oldData, statement, runtime ->
        newData.forEach {
            val avg = it.get("res") as Double? ?: Double.NaN
            val sensor = it.get("sensor") as Int
            averages.add(sensor to avg)
        }
        averages.groupBy { pair -> pair.first }.forEach {
            println("Average history for ${it.key} is: ${it.value.map { it.second }}")
        }
        val streckenabschnitt = sensorsToGetAverageFor.map { id ->
            val value = newData.find { (it.get("sensor") as Int) == id }?.get("res") as Double? ?: Double.NaN
            id to value
        }
        println("Durchschnittsgeschwindigkeiten auf Streckenabschnitt $sensorsToGetAverageFor: $streckenabschnitt")
        if (oldData != null){
            oldData.forEach {
                val oldAvg = it.get("res") as Double? ?: Double.NaN
                val sensor = it.get("sensor") as Int
                val newAvg = newData.find { (it.get("sensor") as Int) == sensor }?.get("res") as Double? ?: Double.NaN
                val speedDrop = oldAvg - newAvg
                if (speedDrop >= 5.0) {
                    runtime.eventService.sendEventBean(SpeedDropEvent(Clock.System.now(), sensor, speedDrop), "SpeedDropEvent")
                }
            }
        }
    }

    val speedDropStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getSpeedDrop")
    speedDropStatement.addListener { newEvents, oldEvents, statement, runtime ->
        val time = newEvents[0].get("time") as Instant
        val sensor = newEvents[0].get("sensor") as Int
        val speedDrop = newEvents[0].get("speedDrop") as Double
        println("ACHTUNG: Um ${time} gab es im Streckenabschnitt von Sensor ${sensor} einen Geschwindigkeitsabfall um ${speedDrop}km/h. Hier könnte sich ein Stau Bilden")
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


