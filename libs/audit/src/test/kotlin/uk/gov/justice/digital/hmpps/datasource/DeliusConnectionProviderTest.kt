package uk.gov.justice.digital.hmpps.datasource

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.context.event.ApplicationStartedEvent
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.User
import uk.gov.justice.digital.hmpps.user.UserService
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

@ExtendWith(MockitoExtension::class)
class DeliusConnectionProviderTest {

    @Mock
    private lateinit var connection: Connection

    @Mock
    private lateinit var preparedStatement: PreparedStatement

    @Mock
    private lateinit var dataSource: DataSource

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var applicationStartedEvent: ApplicationStartedEvent

    private lateinit var serviceContext: ServiceContext

    private val deliusConnectionProvider = DeliusConnectionProvider()

    @Test
    fun `retrieving a connection with oracle sets client identifier with security context`() {
        val user = User(1, "ServiceUserName")
        whenever(userService.findUser(user.username)).thenReturn(user)
        whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
        whenever(dataSource.connection).thenReturn(connection)
        serviceContext = ServiceContext(user.username, userService)
        serviceContext.onApplicationEvent(applicationStartedEvent)
        deliusConnectionProvider.dataSource = dataSource
        deliusConnectionProvider.configure(mapOf<String, Any>())

        deliusConnectionProvider.connection

        verify(dataSource).connection
        verify(connection).prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?)")
        verify(preparedStatement).setString(1, user.username)
        verify(preparedStatement).execute()
    }

    @Test
    fun `close connection successfully removes context in db`() {
        whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)

        deliusConnectionProvider.dataSource = dataSource
        deliusConnectionProvider.configure(mapOf<String, Any>())

        deliusConnectionProvider.closeConnection(connection)

        verify(connection).prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()")
        verify(preparedStatement).execute()
        verify(preparedStatement).close()
    }

    @Test
    fun `when offender id in optimisation context sp called`() {
        OptimisationContext.offenderId.set(765)
        whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
        deliusConnectionProvider.closeConnection(connection)
        verify(connection, times(2)).prepareStatement(anyString())
    }
}
