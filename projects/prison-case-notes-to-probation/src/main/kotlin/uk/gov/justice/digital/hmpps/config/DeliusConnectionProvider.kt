package uk.gov.justice.digital.hmpps.config

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types


class DeliusConnectionProvider : DatasourceConnectionProviderImpl() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun getConnection(): Connection? {
        return super.getConnection()?.also { con ->
            SecurityContextHolder.getContext().authentication?.name?.let { username ->
                try {
                    con.prepareCall("{ ? = call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(?) }").use {
                        it.registerOutParameter(1, Types.BIGINT)
                        it.setString(2, username)
                        it.execute()
                    }
                } catch (e: SQLException) {
                    log.error("Issue with calling PKG_VPD_CTX.SET_CLIENT_IDENTIFIER", e)
                }
            }
        }
    }

    override fun closeConnection(connection: Connection?) {
        connection?.prepareStatement("call PKG_VPD_CTX.CLEAR_CLIENT_IDENTIFIER()").use {
            it?.execute()
            it?.close()
        }
        super.closeConnection(connection)
    }
}