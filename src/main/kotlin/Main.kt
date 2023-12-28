import di.SystemModule
import kafka.KafkaInitilizer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.RandomDataGenerator
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>)  {
    KafkaInitilizer.createTopic("data", 1)
    val consumer = SpeedConsumer()
    val producer = SpeedProducer(RandomDataGenerator(5, 1000,1000, 0.0), "data")
    runBlocking {
        launch {
            while (true) producer.produceData()
            
        }
        launch {
            while (true) {
                delay(5000)
                consumer.getAndConsumeData()
                consumer.calculateAverageSpeedWithSlidingWindow(30.seconds)
            }
        }
    }

}
