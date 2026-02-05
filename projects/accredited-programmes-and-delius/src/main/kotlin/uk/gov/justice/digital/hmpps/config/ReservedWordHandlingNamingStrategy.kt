package uk.gov.justice.digital.hmpps.config

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

/**
 * A custom Hibernate naming strategy that extends the default PhysicalNamingStrategySnakeCaseImpl.
 * It ensures the "system_user" column name is converted to uppercase and quoted so as it is not interpreted as a SQL reserved word.
 */
class ReservedWordHandlingNamingStrategy : PhysicalNamingStrategySnakeCaseImpl() {
    override fun toPhysicalColumnName(logicalName: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return if (logicalName?.canonicalName == "system_user") {
            Identifier("SYSTEM_USER", true);
        } else super.toPhysicalColumnName(logicalName, jdbcEnvironment)
    }
}