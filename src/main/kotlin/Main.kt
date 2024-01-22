import kafka.KafkaInitilizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.beam.runners.direct.DirectRunner
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.*
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.schemas.transforms.Group
import org.apache.beam.sdk.state.BagState
import org.apache.beam.sdk.state.StateSpec
import org.apache.beam.sdk.state.StateSpecs
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.transforms.windowing.AfterPane
import org.apache.beam.sdk.transforms.windowing.FixedWindows
import org.apache.beam.sdk.transforms.windowing.Never
import org.apache.beam.sdk.transforms.windowing.Repeatedly
import org.apache.beam.sdk.transforms.windowing.Window
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection

import org.apache.kafka.common.serialization.StringDeserializer
import org.joda.time.Duration
import org.joda.time.Instant
import utils.Measurement
import utils.RandomDataGenerator
import utils.Utils.round

fun main(args: Array<String>) {
    val options = PipelineOptionsFactory.create()
    options.runner = (DirectRunner::class.java)
    val pipeline = Pipeline.create(options)

    val dataset = pipeline.apply(KafkaIO.read<String, String>()
            .withBootstrapServers("localhost:29092")
            .withTopic("data")
            .withKeyDeserializer(StringDeserializer::class.java)
            .withValueDeserializer(StringDeserializer::class.java)
            .withoutMetadata()
    ).apply(Values.create())
        .setCoder(StringUtf8Coder.of())
        .filterForEmptyValues()
        .filterForNegativeValues()
        .toKMH()
        .extractTimestamp()
        .toKeyValuePair()
        .flatten()
        .calculateAverages()


    dataset.getAverageSpeedOfSensor(1).print()
    dataset.getAverageSpeedOfSensors(1,2,3)



    KafkaInitilizer.createTopic("data", 4)
    val producer = SpeedProducer(RandomDataGenerator(5, 1000, 1000, 0.0), "data")
    runBlocking {
        launch(Dispatchers.IO) {
            while (true) producer.produceData()
        }
        launch(Dispatchers.IO) {
            pipeline.run().waitUntilFinish()
        }
    }
}


fun PCollection<String>.toKMH(): PCollection<String> {
    return this.apply("toKMH", ParDo.of(object : DoFn<String, String>() {
        @ProcessElement
        fun processElement(@Element input: String, out: OutputReceiver<String>) {
            val measurement = Measurement.getFromJSON(input)
            val newMeasurement = Measurement(measurement.time, measurement.sensor, measurement.values.map { (it * 3.6).round(2) })
            out.output(newMeasurement.serialize())
        }
    }))
}

fun <T> PCollection<T>.print(): PCollection<T> {
    return this.apply("Print", ParDo.of(object : DoFn<T, T>() {
        @ProcessElement
        fun processElement(@Element input: T, out: OutputReceiver<T>) {
            println("$input")
            out.output(input)
        }
    }))
}

fun PCollection<String>.filterForEmptyValues(): PCollection<String> {
    return this.apply("FilterEmpty", Filter.by(object : SerializableFunction<String, Boolean> {
        override fun apply(input: String): Boolean {
            return Measurement.getFromJSON(input).values.isNotEmpty()
        }
    }))
}

fun PCollection<String>.filterForNegativeValues(): PCollection<String> {
    return this.apply("FilterOutNegatives", ParDo.of(object : DoFn<String, String>() {
        @ProcessElement
        fun processElement(@Element input: String, out: OutputReceiver<String>) {
            val measurement = Measurement.getFromJSON(input)
            val newMeasurement = Measurement(measurement.time, measurement.sensor, measurement.values.filter { it > 0 })
            out.output(newMeasurement.serialize())
        }
    }))
}

fun PCollection<String>.extractTimestamp(): PCollection<String> {
    return this.apply("ExtractTimestamp", ParDo.of(object : DoFn<String, String>() {

        @ProcessElement
        fun processElement(@Element input: String, out: OutputReceiver<String>) {
            val measurement = Measurement.getFromJSON(input)
            out.outputWithTimestamp(measurement.serialize(), Instant(measurement.time.toEpochMilliseconds() + 1000)) //TODO: find solution for this
        }
    }))
}

fun PCollection<String>.toKeyValuePair(): PCollection<KV<Int, java.util.List<Double>>> {
    return this.apply("TransformToKeyValuePair", ParDo.of(object : DoFn<String, KV<Int, java.util.List<Double>>>() {
        @ProcessElement
        fun processElement(@Element input: String, out: OutputReceiver<KV<Int, List<Double>>>) {
            val measurement = Measurement.getFromJSON(input)
            out.output(KV.of(measurement.sensor, measurement.values))
        }
    }))
}

fun PCollection<KV<Int, java.util.List<Double>>>.flatten(): PCollection<KV<Int, Double>> {
    return this.apply("flatten", ParDo.of(object : DoFn<KV<Int, java.util.List<Double>>, KV<Int, Double>>() {
        @ProcessElement
        fun processElement(c: ProcessContext) {
            val element = c.element()
            element.value.forEach {
                c.output(KV.of(element.key, it))
            }
        }
    }))
}

fun PCollection<KV<Int, Double>>.calculateAverages(): PCollection<KV<Int, Double>> {
    return this.apply("calculateAverages", Window.into<KV<Int, Double>>(FixedWindows.of(Duration.standardSeconds(30)))
        .triggering(Never.ever())
        .withAllowedLateness(Duration.standardSeconds(10), Window.ClosingBehavior.FIRE_ALWAYS)
        .discardingFiredPanes())
    .apply(Mean.perKey())
}

//Aufgabe1
fun PCollection<KV<Int, Double>>.getAverageSpeedOfSensor(sensor: Int): PCollection<KV<Int, Double>> {
    return this.apply("FilterBySensor", Filter.by(SerializableFunction<KV<Int, Double>, Boolean> {
        it.key == sensor
    }))
}

//Aufgabe2

fun PCollection<KV<Int, Double>>.getAverageSpeedOfSensors(vararg sensors: Int): PCollection<Void> {
    return this.apply(Group.globally())
    .apply("calculateAverages",ParDo.of(object : DoFn<Iterable<KV<Int, Double>>, Void>() {
        @ProcessElement
        fun processElement(@Element input: Iterable<KV<Int, Double>>) {
            val step = input.filter { sensors.contains(it.key) }
            println(step)
        }
    }))
}


