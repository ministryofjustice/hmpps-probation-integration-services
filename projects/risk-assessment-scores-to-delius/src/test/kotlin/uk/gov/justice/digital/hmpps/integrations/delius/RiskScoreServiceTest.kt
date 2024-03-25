package uk.gov.justice.digital.hmpps.integrations.delius

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
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

    @Mock
    private lateinit var featureFlags: FeatureFlags

    @Mock
    private lateinit var simpleJdbcCall: SimpleJdbcCall

    @BeforeEach
    fun featureFlag() {
        givenTheOspFlagIs(true)
    }

    @Test
    fun `scores are passed to the database procedure`() {
        givenTheDatabaseProcedureSucceeds().use {
            assertDoesNotThrow {
                whenUpdatingRsrAndOspScores()
                thenProcedureIsCalled()
            }
        }
    }

    @Test
    fun `scores are passed to the old database procedure when flag is disabled`() {
        givenTheOspFlagIs(false)
        givenTheDatabaseProcedureSucceeds().use {
            assertDoesNotThrow {
                whenUpdatingRsrAndOspScores()
                thenTheOldProcedureIsCalled()
            }
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
            assertThat(exception.message, containsString("A validation error we haven't seen before"))
        }
    }

    private fun whenUpdatingRsrAndOspScores() {
        RiskScoreService(jdbcTemplate, featureFlags).updateRsrAndOspScores(
            crn = "A000001",
            eventNumber = 123,
            assessmentDate = ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            rsr = RiskAssessment(1.00, "A"),
            ospIndecent = RiskAssessment(2.00, "B"),
            ospIndirectIndecent = RiskAssessment(3.00, "C"),
            ospContact = RiskAssessment(4.00, "D"),
            ospDirectContact = RiskAssessment(5.00, "E"),
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
            "p_osp_score_c" to 4.00,
            "p_osp_level_i_code" to "B",
            "p_osp_level_c_code" to "D",
            "p_osp_level_iic_code" to "C",
            "p_osp_level_dc_code" to "E",
        )
        verify(simpleJdbcCall).execute(
            check<MapSqlParameterSource> { params ->
                assertThat(params.values, equalTo(expectedValues))
            }
        )
    }

    private fun thenTheOldProcedureIsCalled() {
        val expectedValues = mapOf(
            "p_crn" to "A000001",
            "p_event_number" to 123,
            "p_rsr_assessor_date" to ZonedDateTime.of(2022, 12, 15, 9, 0, 0, 0, EuropeLondon),
            "p_rsr_score" to 1.00,
            "p_rsr_level_code" to "A",
            "p_osp_score_i" to 2.00,
            "p_osp_score_c" to 4.00,
            "p_osp_level_i_code" to "B",
            "p_osp_level_c_code" to "D"
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
        if (featureFlags.enabled("osp-indirect-indecent-and-direct-contact")) {
            whenever(simpleJdbcCall.declareParameters(*Array(2) { any() })).thenReturn(simpleJdbcCall)
        }
        return mockConstructionWithAnswer(SimpleJdbcCall::class.java, { simpleJdbcCall })
    }

    private fun givenTheDatabaseProcedureThrows(e: Throwable): MockedConstruction<SimpleJdbcCall> {
        val mockedConstruction = givenTheDatabaseProcedureSucceeds()
        whenever(simpleJdbcCall.execute(any(SqlParameterSource::class.java))).thenThrow(e)
        return mockedConstruction
    }

    private fun givenTheOspFlagIs(value: Boolean) {
        whenever(featureFlags.enabled("osp-indirect-indecent-and-direct-contact")).thenReturn(value)
    }

    private fun sqlException(message: String? = null, code: Int = 20000) =
        UncategorizedSQLException("error", "sql", SQLException(message, "", code))
}
