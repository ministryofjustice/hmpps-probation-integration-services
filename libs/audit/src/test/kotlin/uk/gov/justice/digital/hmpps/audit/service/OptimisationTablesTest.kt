package uk.gov.justice.digital.hmpps.audit.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionSynchronizationManager

class OptimisationTablesTest {
    @Test
    fun `handles non-Oracle database`() {
        assertDoesNotThrow { OptimisationTables(null).rebuild(123) }
    }

    @Test
    fun `creates afterCommit hook`() {
        val jdbcTemplate = mock(JdbcTemplate::class.java)
        TransactionSynchronizationManager.initSynchronization()

        OptimisationTables(OptimisationTablesRebuild(jdbcTemplate)).rebuild(123)

        assertThat(TransactionSynchronizationManager.getSynchronizations(), hasSize(1))
        TransactionSynchronizationManager.getSynchronizations()[0].afterCommit()
        verify(jdbcTemplate).execute("call PKG_TRIGGERSUPPORT.PROCREBUILDOPTTABLES(123)")
    }
}
