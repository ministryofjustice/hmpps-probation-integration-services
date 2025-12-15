package uk.gov.justice.digital.hmpps.config

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

/**
 * A custom Hibernate naming strategy that extends the default CamelCaseToUnderscoresNamingStrategy.
 * It ensures the "system_user" column name is converted to uppercase and quoted so as it is not interpreted as a SQL reserved word.
 */
class ReservedWordHandlingNamingStrategy : CamelCaseToUnderscoresNamingStrategy() {
    override fun toPhysicalColumnName(logicalName: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return if (logicalName?.canonicalName == "system_user") {
            Identifier("SYSTEM_USER", true);
        } else super.toPhysicalColumnName(logicalName, jdbcEnvironment)
    }
}