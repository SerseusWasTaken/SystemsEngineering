import di.SystemModule

fun main(args: Array<String>) {
    val commandHandler = SystemModule.handler
    val query = SystemModule.queryDatabase
    val eventHandler = SystemModule.eventHandler
    val eventStore = SystemModule.eventStore

    commandHandler.createItem("Item1")
    eventHandler.handleEvent(eventStore.pop())

    commandHandler.createItem("Item2")
    eventHandler.handleEvent(eventStore.pop())

    commandHandler.moveItem("Item1", intArrayOf(1, 3, 0))
    println(query.getItem("Item1")?.location?.get(1))
    println(query.getItem("Item2")?.location?.get(1))
    eventHandler.handleEvent(eventStore.pop())


    commandHandler.moveItem("Item2", intArrayOf(1, 1, 0))
    println(query.getItem("Item1")?.location?.get(1))
    commandHandler.moveItem("Item2", intArrayOf(1, 2, 0))
    eventHandler.handleEvent(eventStore.pop())


    println(query.getItem("Item1")?.location?.get(1))
    println(query.getItem("Item2")?.location?.get(1))
    commandHandler.moveItem("Item1", intArrayOf(1, 2, 0))
    eventHandler.handleEvent(eventStore.pop())
    eventHandler.handleEvent(eventStore.pop())
    println(query.getItem("Item1")?.location?.get(1))
    println(query.getItem("Item2")?.location?.get(1))

    commandHandler.changeValue("Item1", 5)
    eventHandler.handleEvent(eventStore.pop())
    println(query.getItem("Item1")?.value)
    println(query.getItem("Item2")?.moves)

    commandHandler.deleteItem("Item1")
    eventHandler.handleEvent(eventStore.pop())
    println(query.getItem("Item1")?.location?.get(1))
}