import kafka.KafkaInitilizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.apache.beam.runners.direct.DirectRunner
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.coders.VoidCoder
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.Mean
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.Values
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.kafka.common.serialization.StringDeserializer
import utils.Measurement
import utils.RandomDataGenerator
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val options = PipelineOptionsFactory.create()
    options.runner = (DirectRunner::class.java)
    val pipeline = Pipeline.create(options)

    pipeline.apply(KafkaIO.read<String, String>()
            .withBootstrapServers("localhost:29092")
            .withTopic("data")
            .withKeyDeserializer(StringDeserializer::class.java)
            .withValueDeserializer(StringDeserializer::class.java)
            .withoutMetadata()
    ).apply(Values.create())
        .setCoder(StringUtf8Coder.of())
        //TODO(Transforms for A5/1 and A5/2 in Beam)
        .apply("Print", ParDo.of(object : DoFn<String, Void>() {
        @ProcessElement
        fun processElement(@Element input: String) {
            println("${Measurement.getFromJSON(input)}")
        }
    })).setCoder(VoidCoder.of())
    // https://beam.apache.org/documentation/programming-guide/#transforms

    KafkaInitilizer.createTopic("data", 4)
    val consumer = SpeedConsumer(30.seconds)
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
