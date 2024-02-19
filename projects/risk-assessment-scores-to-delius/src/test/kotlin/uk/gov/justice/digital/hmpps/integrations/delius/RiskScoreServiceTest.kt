package uk.gov.justice.digital.hmpps.integrations.delius

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.any
import org.mockito.Mockito.mockConstructionWithAnswer
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.whenever
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.messaging.RiskAssessment
import java.sql.SQLException
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class RiskScoreServiceTest {
    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @Mock
    private lateinit var simpleJdbcCall: SimpleJdbcCall

    @Test
    fun `scores are passed to the database procedure`() {
        givenTheDatabaseProcedureSucceeds().use {
            assertDoesNotThrow {
                whenUpdatingRsrScores()
                thenProcedureIsCalled()
            }
        }
    }

    @Test
    fun `known validation errors are logged to telemetry and wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException("CRN/Offender does not exist", 20000)).use {
            val exception = assertThrows<DeliusValidationError> { whenUpdatingRsrScores() }
            assertThat(exception.message, equalTo("CRN/Offender does not exist"))
        }
    }

    @Test
    fun `unknown errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(RuntimeException("unknown error")).use {
            val exception = assertThrows<RuntimeException> { whenUpdatingRsrScores() }
            assertThat(exception.message, equalTo("unknown error"))
        }
    }

    @Test
    fun `unknown SQL errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException()).use {
            val exception = assertThrows<UncategorizedSQLException> { whenUpdatingRsrScores() }
            assertThat(exception.message, containsString("uncategorized SQLException"))
        }
    }

    @Test
    fun `unknown validation errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException("A validation error we haven't seen before", 20000)).use {
            val exception = assertThrows<UncategorizedSQLException> { whenUpdatingRsrScores() }
            assertThat(exception.message, containsString("A validation error we haven't seen before"))
        }
    }

    private fun whenUpdatingRsrScores() {
        RiskScoreService(jdbcTemplate).updateRsrScores(
            "A000001",
            123,
            ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            RiskAssessment(1.00, "A"),
            RiskAssessment(2.00, "B"),
            RiskAssessment(3.00, "C")
        )
    }

    private fun thenProcedureIsCalled() {
        val expectedValues = mapOf(
            "p_crn" to "A000001",
            "p_event_number" to 123,
            "p_rsr_assessor_date" to ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            "p_rsr_score" to 1.00,
            "p_rsr_level_code" to "A",
            "p_osp_score_i" to 2.00,
            "p_osp_score_c" to 3.00,
            "p_osp_level_i_code" to "B",
            "p_osp_level_c_code" to "C"
        )
        verify(simpleJdbcCall).execute(
            check<MapSqlParameterSource> { params ->
                assertThat(params.values, equalTo(expectedValues))
            }
        )
    }

    private fun givenTheDatabaseProcedureSucceeds(): MockedConstruction<SimpleJdbcCall> {
        whenever(simpleJdbcCall.withProcedureName("procUpdateCAS")).thenReturn(simpleJdbcCall)
        whenever(simpleJdbcCall.withoutProcedureColumnMetaDataAccess()).thenReturn(simpleJdbcCall)
        whenever(simpleJdbcCall.declareParameters(*Array(9) { any() })).thenReturn(simpleJdbcCall)
        return mockConstructionWithAnswer(SimpleJdbcCall::class.java, { simpleJdbcCall })
    }

    private fun givenTheDatabaseProcedureThrows(e: Throwable): MockedConstruction<SimpleJdbcCall> {
        val mockedConstruction = givenTheDatabaseProcedureSucceeds()
        whenever(simpleJdbcCall.execute(any(SqlParameterSource::class.java))).thenThrow(e)
        return mockedConstruction
    }

    private fun sqlException(message: String? = null, code: Int = 20000) =
        UncategorizedSQLException("error", "sql", SQLException(message, "", code))
}
