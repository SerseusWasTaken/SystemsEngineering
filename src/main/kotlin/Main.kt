import com.espertech.esper.common.client.configuration.Configuration
import com.espertech.esper.compiler.client.CompilerArguments
import com.espertech.esper.compiler.client.EPCompilerProvider
import com.espertech.esper.runtime.client.EPRuntimeProvider
import esper.FlattenedMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

import utils.Measurement
import utils.RandomDataGenerator

fun main(args: Array<String>) {

    val config = Configuration()
    config.common.addEventType(Measurement::class.java)
    config.common.addEventType(FlattenedMeasurement::class.java)
    val compiler = EPCompilerProvider.getCompiler()
    val compilerArgs = CompilerArguments(config)
    val q1 = "@name('getMeasurements') select time, sensor, doubleList from Measurement;\n"
    val q2 = "@name('getFlattenedMeasurements') select * from FlattenedMeasurement;\n"
    val q3 = "@name('getAverage') select avg(speed) as res from FlattenedMeasurement#ext_timed(time, 4 sec);\n"
    val epCompiled = compiler.compile(q1 + q2 + q3,compilerArgs)

    val runtime = EPRuntimeProvider.getDefaultRuntime(config)
    runtime.initialize()
    val deployment = runtime.deploymentService.deploy(epCompiled)

    val getMeasurementsStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getMeasurements")
    getMeasurementsStatement.addListener { newData, oldData, statement, runtime ->
        val time: Instant = newData[0].get("time") as Instant
        val sensor = newData[0].get("sensor") as Int
        val values = newData[0].get("doubleList") as List<Double>
        val m = Measurement(time, sensor, values)
        val flattened = m.doubleList.map { FlattenedMeasurement(m.time.toEpochMilliseconds(), m.sensor, it) }
        flattened.forEach { runtime.eventService.sendEventBean(it, "FlattenedMeasurement") }
        println(m)
    }
    val getFlattenedStatement = runtime.deploymentService.getStatement(deployment.deploymentId, "getFlattenedMeasurements")
    getFlattenedStatement.addListener { newData, oldData, statement, runtime ->
        val time = newData[0].get("time") as Long
        val sensor = newData[0].get("sensor") as Int
        val values = newData[0].get("speed") as Double
        val m = FlattenedMeasurement(time, sensor, values)
        println(m)
    }

    val getAverageStatment = runtime.deploymentService.getStatement(deployment.deploymentId, "getAverage")
    getAverageStatment.addListener { newData, oldData, statement, runtime ->
        val avg = newData[0].get("res") as Double
        println(avg)
    }




    //Aktuell ohne Kafka
    //KafkaInitilizer.createTopic("data", 4)
    val producer = SpeedProducer(RandomDataGenerator(5, 1000, 1000, 0.0), runtime)
    runBlocking {
        launch(Dispatchers.IO) {
            while (true) producer.produceData()
        }
        launch(Dispatchers.IO) {
        }
    }


}


