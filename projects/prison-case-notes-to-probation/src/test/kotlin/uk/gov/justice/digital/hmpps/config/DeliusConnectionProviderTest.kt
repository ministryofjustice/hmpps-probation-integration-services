package uk.gov.justice.digital.hmpps.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.datasource.DeliusConnectionProvider
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
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

    private lateinit var serviceContext: ServiceContext

    private val deliusConnectionProvider = DeliusConnectionProvider()

    @Test
    fun `retrieving a connection with oracle sets client identifier with security context`() {
        val user = UserGenerator.APPLICATION_USER
        serviceContext = ServiceContext(user.username, userService)
        whenever(userService.findUser(user.username)).thenReturn(user)
        whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
        whenever(dataSource.connection).thenReturn(connection)
        serviceContext.setUp()
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
}
