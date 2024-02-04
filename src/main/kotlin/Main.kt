import di.SystemModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.security.AuthenticationUser
import org.apache.activemq.security.SimpleAuthenticationPlugin

fun main(args: Array<String>) = runBlocking {
    val broker = initBroker()
    val commandHandler = SystemModule.handler
    val queryDatabase = SystemModule.queryDatabase
    val eventHandler = SystemModule.eventHandler
    val eventStore = SystemModule.eventStore


    GlobalScope.launch {
        while (true) {
            eventHandler.fetchEvent()
        }
    }


    commandHandler.createItem("Item1")
    //eventHandler.fetchEvent()

    commandHandler.createItem("Item2", intArrayOf(1,1,1), 0)
    //eventHandler.fetchEvent()

    commandHandler.moveItem("Item1", intArrayOf(1, 3, 0))
    println("Location of Item1: ${queryDatabase.getItem("Item1")?.location?.toList()}")
    println("Location of Item2: ${queryDatabase.getItem("Item2")?.location?.toList()}")
    //eventHandler.fetchEvent()


    commandHandler.moveItem("Item2", intArrayOf(1, 1, 0))
    println("Location of Item1: ${queryDatabase.getItem("Item1")?.location?.toList()}")
    commandHandler.moveItem("Item2", intArrayOf(1, 2, 0))
    // eventHandler.fetchEvent()


    println("Location of Item1: ${queryDatabase.getItem("Item1")?.location?.toList()}")
    println("Location of Item2: ${queryDatabase.getItem("Item2")?.location?.toList()}")
    commandHandler.moveItem("Item1", intArrayOf(1, 2, 0))
    //eventHandler.fetchEvent()
    //eventHandler.fetchEvent()
    println("Location of Item1: ${queryDatabase.getItem("Item1")?.location?.toList()}")
    println("Location of Item2: ${queryDatabase.getItem("Item2")?.location?.toList()}")

    commandHandler.changeValue("Item1", 5)
    //eventHandler.fetchEvent()
    println("Value of Item1 is: ${queryDatabase.getItem("Item1")?.value}")
    println("Value of Item1 is: ${queryDatabase.getItem("Item2")?.value}")

    commandHandler.deleteItem("Item1")
    //eventHandler.fetchEvent()
    println("Location of Item1: ${queryDatabase.getItem("Item1")?.location?.toList()}")

    println("Average receive time: ${eventHandler.getAverageRecieveTime()}")

    broker.stop()
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
