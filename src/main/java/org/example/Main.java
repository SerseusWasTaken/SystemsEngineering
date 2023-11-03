package org.example;

import org.example.di.SystemModule;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        var commandHandler = SystemModule.handler;
        var query = SystemModule.queryModel;
        var eventHandler = SystemModule.eventHandler;
        var eventStore = SystemModule.eventStore;

        /*
        Thread t = new Thread()
        {
            public void run() {
                try {
                    while(true)
                        eventHandler.fetchEvent();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        t.start();

         */



        commandHandler.createItem("Item1");
        eventHandler.handleEvent(eventStore.pop());

        commandHandler.createItem("Item2");
        eventHandler.handleEvent(eventStore.pop());

        commandHandler.moveItem("Item1", new int[] {1, 3, 0});
        System.out.println(query.getItem("Item1").getLocation()[1]);
        System.out.println(query.getItem("Item2").getLocation()[1]);
        eventHandler.handleEvent(eventStore.pop());


        commandHandler.moveItem("Item2", new int[] {1, 1, 0});
        System.out.println(query.getItem("Item1").getLocation()[1]);
        commandHandler.moveItem("Item2", new int[] {1, 2, 0});
        eventHandler.handleEvent(eventStore.pop());


        System.out.println(query.getItem("Item1").getLocation()[1]);
        System.out.println(query.getItem("Item2").getLocation()[1]);
        commandHandler.moveItem("Item1", new int[] {1, 2, 0});
        eventHandler.handleEvent(eventStore.pop());
        eventHandler.handleEvent(eventStore.pop());
        System.out.println(query.getItem("Item1").getLocation()[1]);
        System.out.println(query.getItem("Item2").getLocation()[1]);

        commandHandler.changeValue("Item1", 5);
        eventHandler.handleEvent(eventStore.pop());
        System.out.println(query.getItem("Item1").getValue());
        System.out.println(query.getItem("Item2").getNumberOfMoves());

        commandHandler.deleteItem("Item1");
        eventHandler.handleEvent(eventStore.pop());
        System.out.println(query.getItem("Item1").getLocation()[1]);

    }
}