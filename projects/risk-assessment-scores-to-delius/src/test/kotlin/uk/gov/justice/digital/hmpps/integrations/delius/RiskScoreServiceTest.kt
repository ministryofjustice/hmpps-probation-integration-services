package uk.gov.justice.digital.hmpps.integrations.delius

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.Mockito.withSettings
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.messaging.RiskAssessment
import java.sql.SQLException
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class RiskScoreServiceTest {

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `scores are passed to the database procedure`() {
        givenTheDatabaseProcedureSucceeds().use { construction ->
            assertDoesNotThrow {
                whenUpdatingRsrAndOspScores()
            }
            thenProcedureIsCalled(construction)
        }
    }

    @Test
    fun `known validation errors are logged to telemetry and wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException("CRN/Offender does not exist", 20000)).use {
            val exception = assertThrows<DeliusValidationError> { whenUpdatingRsrAndOspScores() }
            assertThat(exception.message, equalTo("CRN/Offender does not exist"))
        }
    }

    @Test
    fun `unknown errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(RuntimeException("unknown error")).use {
            val exception = assertThrows<RuntimeException> { whenUpdatingRsrAndOspScores() }
            assertThat(exception.message, equalTo("unknown error"))
        }
    }

    @Test
    fun `unknown SQL errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException()).use {
            val exception = assertThrows<UncategorizedSQLException> { whenUpdatingRsrAndOspScores() }
            assertThat(exception.message, containsString("uncategorized SQLException"))
        }
    }

    @Test
    fun `unknown validation errors are not wrapped`() {
        givenTheDatabaseProcedureThrows(sqlException("A validation error we haven't seen before", 20000)).use {
            val exception = assertThrows<UncategorizedSQLException> { whenUpdatingRsrAndOspScores() }
            // The message of UncategorizedSQLException usually includes the cause text
            assertThat(exception.message, containsString("A validation error we haven't seen before"))
        }
    }

    // ---- Helpers ----

    private fun whenUpdatingRsrAndOspScores() {

        RiskScoreService(jdbcTemplate).updateRsrAndOspScores(
            crn = "A000001",
            eventNumber = 123,
            assessmentDate = ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            rsr = RiskAssessment.V3(1.00, "A", "STATIC"),
            ospIndecent = RiskAssessment.V3(2.00, "B"),
            ospIndirectIndecent = RiskAssessment.V3(3.00, "C"),
            ospContact = RiskAssessment.V3(4.00, "D"),
            ospDirectContact = RiskAssessment.V3(5.00, "E"),
        )
    }

    private fun thenProcedureIsCalled(construction: MockedConstruction<SimpleJdbcCall>) {
        val expectedValues = mapOf(
            "p_crn" to "A000001",
            "p_event_number" to 123,
            "p_rsr_assessor_date" to ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            "p_rsr_score" to 1.00,
            "p_rsr_level_code" to "A",
            "p_osp_score_i" to 2.00,
            "p_osp_score_c" to 4.00,
            "p_osp_level_i_code" to "B",
            "p_osp_level_c_code" to "D",
            "p_osp_level_iic_code" to "C",
            "p_osp_level_dc_code" to "E",
            "p_rsr_static_flag" to "STATIC",
        )

        // Find which constructed instance actually received execute(...)
        val executed = construction.constructed().firstOrNull { constructed ->
            Mockito.mockingDetails(constructed).invocations.any { it.method.name == "execute" }
        } ?: fail("No SimpleJdbcCall.execute(...) was invoked")

        verify(executed).execute(
            check<MapSqlParameterSource> { params ->
                assertThat(params.values, equalTo(expectedValues))
            }
        )
    }

    /**
     * Mocks construction of **all** SimpleJdbcCall instances created inside the SUT.
     * Sets default answer to RETURNS_SELF so fluent builder calls chain without explicit stubs.
     * Stubs execute(...) to return an empty map (success).
     */
    private fun givenTheDatabaseProcedureSucceeds(): MockedConstruction<SimpleJdbcCall> =
        Mockito.mockConstruction(
            SimpleJdbcCall::class.java,
            withSettings().defaultAnswer(Answers.RETURNS_SELF)
        ) { mock, _ ->
            whenever(mock.execute(any<MapSqlParameterSource>())).thenReturn(mutableMapOf())
        }

    /**
     * Same as above, but execute(...) throws the provided Throwable for **every** constructed instance.
     */
    private fun givenTheDatabaseProcedureThrows(e: Throwable): MockedConstruction<SimpleJdbcCall> =
        Mockito.mockConstruction(
            SimpleJdbcCall::class.java,
            withSettings().defaultAnswer(Answers.RETURNS_SELF)
        ) { mock, _ ->
            whenever(mock.execute(any<SqlParameterSource>())).thenThrow(e)
        }

    /**
     * Build an UncategorizedSQLException with a nested SQLException carrying the message and code.
     * We set the outer message to "uncategorized SQLException" so tests can assert on it.
     */
    private fun sqlException(message: String? = null, code: Int = 20000): UncategorizedSQLException =
        UncategorizedSQLException(
            "uncategorized SQLException",
            "sql",
            SQLException(message, "", code)
        )
}
