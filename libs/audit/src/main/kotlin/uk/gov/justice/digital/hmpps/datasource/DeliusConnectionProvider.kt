package uk.gov.justice.digital.hmpps.datasource

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.sql.Connection

open class DeliusConnectionProvider : DatasourceConnectionProviderImpl() {
    override fun getConnection(): Connection {
        return super.getConnection().also { con ->
            ServiceContext.servicePrincipal().username.let { username ->
                con.prepareStatement("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?)").use {
                    it.setString(1, username)
                    it.execute()
                }
            }
        }
    }

    override fun closeConnection(connection: Connection) {
        if (OptimisationContext.offenderId.get() != null) {
            connection.prepareStatement("call PKG_TRIGGERSUPPORT.PROCREBUILDOPTTABLES(${OptimisationContext.offenderId.get()})")
                .use {
                    it.execute()
                }
            OptimisationContext.offenderId.set(null)
        }
        connection.prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()").use {
            it.execute()
        }
        super.closeConnection(connection)
    }
}
