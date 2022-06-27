package uk.gov.justice.digital.hmpps.config

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import java.sql.Connection

open class DeliusConnectionProvider : DatasourceConnectionProviderImpl() {
    override fun getConnection(): Connection {
        return super.getConnection().also { con ->
            (SecurityContextHolder.getContext().authentication?.principal as ServicePrincipal?)?.clientId?.let { username ->
                con.prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?)").use {
                    it.setString(1, username)
                    it.execute()
                }
            }
        }
    }

    override fun closeConnection(connection: Connection) {
        connection.prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()").use {
            it.execute()
        }
        super.closeConnection(connection)
    }
}
