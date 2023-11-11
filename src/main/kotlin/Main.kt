import di.SystemModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking{
    val commandHandler = SystemModule.handler
    val queryDatabase = SystemModule.queryDatabase
    val eventHandler = SystemModule.eventHandler
    val eventStore = SystemModule.eventStore

    /*
    GlobalScope.launch {
        while (true) {
            eventHandler.fetchEvent()
        }
    }
     */

    commandHandler.createItem("Item1")
    eventHandler.handleEvent(eventStore.pop())

    commandHandler.createItem("Item2")
    eventHandler.handleEvent(eventStore.pop())

    commandHandler.moveItem("Item1", intArrayOf(1, 3, 0))
    println(queryDatabase.getItem("Item1")?.location?.get(1))
    println(queryDatabase.getItem("Item2")?.location?.get(1))
    eventHandler.handleEvent(eventStore.pop())


    commandHandler.moveItem("Item2", intArrayOf(1, 1, 0))
    println(queryDatabase.getItem("Item1")?.location?.get(1))
    commandHandler.moveItem("Item2", intArrayOf(1, 2, 0))
    eventHandler.handleEvent(eventStore.pop())


    println(queryDatabase.getItem("Item1")?.location?.get(1))
    println(queryDatabase.getItem("Item2")?.location?.get(1))
    commandHandler.moveItem("Item1", intArrayOf(1, 2, 0))
    eventHandler.handleEvent(eventStore.pop())
    eventHandler.handleEvent(eventStore.pop())
    println(queryDatabase.getItem("Item1")?.location?.get(1))
    println(queryDatabase.getItem("Item2")?.location?.get(1))

    commandHandler.changeValue("Item1", 5)
    eventHandler.handleEvent(eventStore.pop())
    println(queryDatabase.getItem("Item1")?.value)
    println(queryDatabase.getItem("Item2")?.moves)

    commandHandler.deleteItem("Item1")
    eventHandler.handleEvent(eventStore.pop())
    println(queryDatabase.getItem("Item1")?.location?.get(1))
}