package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.SqlParameter
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.messaging.RiskAssessment
import java.sql.SQLException
import java.sql.Types
import java.time.ZonedDateTime

@Service
class RiskScoreService(jdbcTemplate: JdbcTemplate) {
    private val updateRsrScoresProcedure =
        SimpleJdbcCall(jdbcTemplate)
            .withCatalogName("pkg_triggersupport")
            .withProcedureName("procUpdateCAS")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                SqlParameter("p_crn", Types.VARCHAR),
                SqlParameter("p_event_number", Types.NUMERIC),
                SqlParameter("p_rsr_assessor_date", Types.DATE),
                SqlParameter("p_rsr_score", Types.NUMERIC),
                SqlParameter("p_rsr_level_code", Types.VARCHAR),
                SqlParameter("p_osp_score_i", Types.NUMERIC),
                SqlParameter("p_osp_score_c", Types.NUMERIC),
                SqlParameter("p_osp_level_i_code", Types.VARCHAR),
                SqlParameter("p_osp_level_c_code", Types.VARCHAR),
            )

    fun updateRsrScores(
        crn: String,
        eventNumber: Int?,
        assessmentDate: ZonedDateTime,
        rsr: RiskAssessment,
        ospIndecent: RiskAssessment,
        ospContact: RiskAssessment,
    ) {
        try {
            updateRsrScoresProcedure.execute(
                MapSqlParameterSource()
                    .addValue("p_crn", crn)
                    .addValue("p_event_number", eventNumber)
                    .addValue("p_rsr_assessor_date", assessmentDate)
                    .addValue("p_rsr_score", rsr.score)
                    .addValue("p_rsr_level_code", rsr.band)
                    .addValue("p_osp_score_i", ospIndecent.score)
                    .addValue("p_osp_score_c", ospContact.score)
                    .addValue("p_osp_level_i_code", ospIndecent.band)
                    .addValue("p_osp_level_c_code", ospContact.band),
            )
        } catch (e: UncategorizedSQLException) {
            e.sqlException.takeIf { it.isValidationError() }
                ?.parsedValidationMessage()
                ?.takeIf { it.isDeliusValidationMessage() }
                ?.let { throw DeliusValidationError(it) }
            throw e
        }
    }

    private fun SQLException.isValidationError() = errorCode == 20000

    private fun SQLException.parsedValidationMessage() =
        message
            ?.replace(Regex("\\n.*"), "") // take the first line
            ?.replace(Regex("\\[[^]]++]\\s*"), "") // remove anything inside square brackets
            ?.removePrefix("ORA-20000: INTERNAL ERROR: An unexpected error in PL/SQL: ERROR : ") // remove Oracle prefix
            ?.trim()

    private fun String.isDeliusValidationMessage() = DeliusValidationError.isKnownValidationMessage(this)
}
