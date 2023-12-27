package integration

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.unmockkAll
import io.mockk.verify
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.security.AuthenticationUser
import org.apache.activemq.security.SimpleAuthenticationPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll


class IntegrationTest {
    // Tests are broken right now

    @AfterEach
    fun teardown() {
        unmockkAll()
        clearAllMocks()
    }


    companion object {
        lateinit var broker: BrokerService
        @BeforeAll
        @JvmStatic
        fun setup() {
            broker = BrokerService()
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
        }
    }
}
