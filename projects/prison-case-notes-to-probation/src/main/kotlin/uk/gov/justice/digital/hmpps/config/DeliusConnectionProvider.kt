package uk.gov.justice.digital.hmpps.config

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import org.springframework.security.core.context.SecurityContextHolder
import java.sql.Connection
import java.sql.Types

open class DeliusConnectionProvider : DatasourceConnectionProviderImpl() {
    override fun getConnection(): Connection {
        return super.getConnection().also { con ->
            SecurityContextHolder.getContext().authentication?.name?.let { username ->
                con.prepareCall("{ ? = call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?) }").use {
                    it.registerOutParameter(1, Types.BIGINT)
                    it.setString(2, username)
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
