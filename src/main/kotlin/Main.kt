import di.SystemModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.common.KafkaEnvironment
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.security.AuthenticationUser
import org.apache.activemq.security.SimpleAuthenticationPlugin

val environment = KafkaEnvironment(
    topicNames = listOf("allEvents"),
    //autoStart = true,
    noOfBrokers = 1
)
fun main(args: Array<String>) = runBlocking {
    environment.start()
    val commandHandler = SystemModule.handler
    val queryDatabase = SystemModule.queryDatabase
    val eventHandler = SystemModule.eventHandler


    GlobalScope.launch {
        while (true) {
            eventHandler.fetchEvent()
        }
    }

     
    runBlocking {
        delay(10000L)
        commandHandler.createItem("Item1")
        delay(1000)
        commandHandler.moveItem("Item1", intArrayOf(1,2,3))
        delay(1000)
        println(queryDatabase.getItem("Item1")?.location?.toList())
        delay(1000)
        commandHandler.moveItem("Item1", intArrayOf(1,2,3))
        delay(1000)
        println(queryDatabase.getItem("Item1")?.location?.toList())
        delay(1000)
        //Should result in a replace event
        commandHandler.createItem("Item2", intArrayOf(2,4,6), 0)
        delay(1000)
        //Expect null here
        println(queryDatabase.getItem("Item1")?.location?.toList())
        delay(1000)
        commandHandler.createItem("Item2", intArrayOf(2,4,7), 0)
        delay(1000)
        commandHandler.createItem("Item2", intArrayOf(2,4,7), 0)
    }


}

fun initBroker(): BrokerService {
    val broker = BrokerService()
    broker.brokerName = "MovingItemBroker"
    broker.setDataDirectory("brokerData/")
    val auth = SimpleAuthenticationPlugin()
    auth.setUsers(
        listOf(
            AuthenticationUser("query", "query", "consumers"),
            AuthenticationUser("command", "command", "publishers")
        )
    )
    broker.plugins = arrayOf(auth)
    broker.addConnector("tcp://localhost:61616")
    broker.addConnector("tcp://localhost:1883")
    broker.start()
    return broker
}
