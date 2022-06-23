package uk.gov.justice.digital.hmpps.config

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types
import javax.sql.DataSource

@ExtendWith(MockitoExtension::class)
class DeliusConnectionProviderTest {

    @Mock
    private lateinit var authentication: Authentication

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var connection: Connection

    @Mock
    private lateinit var callableStatement: CallableStatement

    @Mock
    private lateinit var preparedStatement: PreparedStatement

    @Mock
    private lateinit var dataSource: DataSource

    private val deliusConnectionProvider = DeliusConnectionProvider()

    @Test
    fun `retrieving a connection with oracle sets client identifier with security context`() {
        val username = UserGenerator.APPLICATION_USER.username
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.name).thenReturn(username)
        whenever(connection.prepareCall(anyString())).thenReturn(callableStatement)
        whenever(dataSource.connection).thenReturn(connection)

        SecurityContextHolder.setContext(securityContext)
        deliusConnectionProvider.dataSource = dataSource
        deliusConnectionProvider.configure(mapOf<String, Any>())

        deliusConnectionProvider.connection

        verify(dataSource).connection
        verify(connection).prepareCall("{ ? = call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?) }")
        verify(callableStatement).registerOutParameter(1, Types.BIGINT)
        verify(callableStatement).setString(2, username)
        verify(callableStatement).execute()
    }

    @Test
    fun `if no security context no errors are thrown and connection instance is returned`() {
        whenever(securityContext.authentication).thenReturn(null)
        whenever(dataSource.connection).thenReturn(connection)

        SecurityContextHolder.setContext(securityContext)
        deliusConnectionProvider.dataSource = dataSource
        deliusConnectionProvider.configure(mapOf<String, Any>())

        val con = deliusConnectionProvider.connection
        verify(dataSource).connection
        verify(connection, times(0)).prepareCall("{ ? = call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?) }")
        assertThat(con, equalTo(connection))
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
