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
    val eventStore = SystemModule.eventStore


    GlobalScope.launch {
        while (true) {
            println("Do loop")
            eventHandler.fetchEvent()
        }
    }

     
    runBlocking {
        delay(10000L)
        commandHandler.createItem("Item1")
        //eventHandler.fetchEvent()

        commandHandler.createItem("Item2", intArrayOf(1,1,1), 0)
        //eventHandler.fetchEvent()

        commandHandler.moveItem("Item1", intArrayOf(1, 3, 0))
        println(queryDatabase.getItem("Item1")?.location?.get(1))
        println(queryDatabase.getItem("Item2")?.location?.get(1))
        //eventHandler.fetchEvent()


        commandHandler.moveItem("Item2", intArrayOf(1, 1, 0))
        println(queryDatabase.getItem("Item1")?.location?.get(1))
        commandHandler.moveItem("Item2", intArrayOf(1, 2, 0))
        // //eventHandler.fetchEvent()


        println(queryDatabase.getItem("Item1")?.location?.get(1))
        println(queryDatabase.getItem("Item2")?.location?.get(1))
        commandHandler.moveItem("Item1", intArrayOf(1, 2, 0))
        //eventHandler.fetchEvent()
        //eventHandler.fetchEvent()
        println(queryDatabase.getItem("Item1")?.location?.get(1))
        println(queryDatabase.getItem("Item2")?.location?.get(1))

        commandHandler.changeValue("Item1", 5)
        //eventHandler.fetchEvent()
        println(queryDatabase.getItem("Item1")?.value)
        println(queryDatabase.getItem("Item2")?.moves)



        delay(2000)
        println(queryDatabase.getItem("Item1"))
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
