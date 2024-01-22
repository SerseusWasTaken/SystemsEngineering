package integration

import com.espertech.esper.common.client.configuration.Configuration
import com.espertech.esper.common.client.scopetest.EPAssertionUtil
import com.espertech.esper.compiler.client.CompilerArguments
import com.espertech.esper.compiler.client.EPCompileException
import com.espertech.esper.compiler.client.EPCompilerProvider
import com.espertech.esper.runtime.client.EPDeployException
import com.espertech.esper.runtime.client.EPRuntime
import com.espertech.esper.runtime.client.EPRuntimeProvider
import com.espertech.esper.runtime.client.scopetest.SupportUpdateListener
import esper.AverageSpeed
import esper.FlattenedMeasurement
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test


class IntegrationTest {

    @Throws(EPDeployException::class, EPCompileException::class)
    fun setStatement(statement: String): SupportUpdateListener {
        val compiled = EPCompilerProvider.getCompiler().compile(statement, compilerArgs)
        val stmt = runtime.deploymentService.deploy(compiled).statements[0]
        val listener = SupportUpdateListener()
        stmt.addListener(listener)
        return listener
    }

    @Test
    fun `should calculate average correctly`(): Unit = runBlocking {
        val q5 = "@name('getAverage') select avg(speed) as averageSpeed, sensor from FlattenedMeasurement group by sensor having count(speed) > 0;\n"
        val listener = setStatement(q5)
        val time = Clock.System.now()
        runtime.eventService.sendEventBean(
            FlattenedMeasurement(time.toEpochMilliseconds(), 1, 20.0),
            "FlattenedMeasurement"
        )
        runtime.eventService.sendEventBean(
            FlattenedMeasurement(time.toEpochMilliseconds() + 1, 1, 40.0),
            "FlattenedMeasurement"
        )
        EPAssertionUtil.assertProps(
            listener.lastNewData[0],
            arrayOf("averageSpeed", "sensor"), arrayOf(30.0, 1))
    }

    companion object {
        val config = Configuration()
        lateinit var compilerArgs: CompilerArguments
        lateinit var runtime: EPRuntime
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            config.common.addEventType(FlattenedMeasurement::class.java)
            config.common.addEventType(AverageSpeed::class.java)
            compilerArgs = CompilerArguments(config)
            runtime = EPRuntimeProvider.getDefaultRuntime(config)
        }
    }
}
