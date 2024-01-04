import kafka.KafkaInitilizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import utils.RandomDataGenerator
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>)  {
    KafkaInitilizer.createTopic("data", 4)
    val consumer = SpeedConsumer(30.seconds)
    val producer = SpeedProducer(RandomDataGenerator(5, 1000,1000, 0.0), "data")
    runBlocking {
        launch {
            while (true) producer.produceData()
        }
        launch {
            while (true) {
                delay(5000)
                consumer.getAndConsumeData()
                consumer.calculateAverageSpeedWithinTimeWindow()
            }
        }
        launch {
            while (true) {
                delay(40.seconds)
                println("Average over time for 1,2,3: ${consumer.getAverageOfRoad(Clock.System.now() - 25.seconds, 1,2,3)}")
            }
        }
    }
}
