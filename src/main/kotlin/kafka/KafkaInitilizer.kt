package kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import java.util.*
import java.util.concurrent.ExecutionException


object KafkaInitilizer {
    val server = "localhost:29092"
    fun createTopic(topicName: String, numPartitions: Int) {
        val config = Properties()
        config[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = server
        try {
            AdminClient.create(config).use { adminClient ->
                // Create a new topic with one partition
                val newTopic = NewTopic(topicName, numPartitions, 1.toShort())
                adminClient.createTopics(setOf(newTopic)).all().get()
                println("Topic $topicName created successfully with $numPartitions partition.")
            }
        } catch (e: InterruptedException) {
            System.err.println("Error creating topic: " + e.message)
        } catch (e: ExecutionException) {
            System.err.println("Error creating topic: " + e.message)
        }
    }
}