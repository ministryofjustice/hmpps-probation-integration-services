package uk.gov.justice.digital.hmpps.audit.service

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class OptimisationTables(private val optimisationTablesRebuild: OptimisationTablesRebuild?) {
    fun rebuild(personId: Long) {
        optimisationTablesRebuild?.rebuild(personId)
    }
}

@Service
@Conditional(OracleCondition::class)
class OptimisationTablesRebuild(private val jdbcTemplate: JdbcTemplate) {
    fun rebuild(personId: Long) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                jdbcTemplate.execute("call PKG_TRIGGERSUPPORT.PROCREBUILDOPTTABLES($personId)")
            }
        })
    }
}

class OracleCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val url = context.environment.getProperty("spring.datasource.url")
        return url?.startsWith("jdbc:oracle") ?: false && !context.environment.acceptsProfiles { it.test("oracle") }
    }
}
