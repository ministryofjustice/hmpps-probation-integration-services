package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.SqlParameter
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.messaging.RiskAssessment
import java.sql.SQLException
import java.sql.Types
import java.time.ZonedDateTime

@Service
class RiskScoreService(jdbcTemplate: JdbcTemplate, val featureFlags: FeatureFlags) {
    private val updateRsrAndOspScoresProcedure = SimpleJdbcCall(jdbcTemplate)
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

    private val SimpleJdbcCall.withIndirectIndecentAndDirectContact
        get() = declareParameters(
            SqlParameter("p_osp_level_iic_code", Types.VARCHAR),
            SqlParameter("p_osp_level_dc_code", Types.VARCHAR),
        )

    fun updateRsrAndOspScores(
        crn: String,
        eventNumber: Int?,
        assessmentDate: ZonedDateTime,
        rsr: RiskAssessment,
        ospIndecent: RiskAssessment?,
        ospIndirectIndecent: RiskAssessment?,
        ospContact: RiskAssessment?,
        ospDirectContact: RiskAssessment?,
    ) {
        try {
            val params = MapSqlParameterSource()
                .addValue("p_crn", crn)
                .addValue("p_event_number", eventNumber)
                .addValue("p_rsr_assessor_date", assessmentDate)
                .addValue("p_rsr_score", rsr.score)
                .addValue("p_rsr_level_code", rsr.band)
                .addValue("p_osp_score_i", ospIndecent?.score)
                .addValue("p_osp_score_c", ospContact?.score)
                .addValue("p_osp_level_i_code", ospIndecent?.band)
                .addValue("p_osp_level_c_code", ospContact?.band)

            if (featureFlags.enabled("osp-indirect-indecent-and-direct-contact")) {
                updateRsrAndOspScoresProcedure.withIndirectIndecentAndDirectContact.execute(
                    params
                        .addValue("p_osp_level_iic_code", ospIndirectIndecent?.band)
                        .addValue("p_osp_level_dc_code", ospDirectContact?.band)
                )
            } else {
                updateRsrAndOspScoresProcedure.execute(params)
            }
        } catch (e: UncategorizedSQLException) {
            e.sqlException.takeIf { it.isValidationError }
                ?.parsedValidationMessage
                ?.takeIf { it.isDeliusValidationMessage }
                ?.let { throw DeliusValidationError(it) }
            throw e
        }
    }

    private val SQLException.isValidationError get() = errorCode == 20000
    private val SQLException.parsedValidationMessage
        get() = message
            ?.replace(Regex("\\n.*"), "") // take the first line
            ?.replace(Regex("\\[[^]]++]\\s*"), "") // remove anything inside square brackets
            ?.removePrefix("ORA-20000: INTERNAL ERROR: An unexpected error in PL/SQL: ERROR : ") // remove Oracle prefix
            ?.trim()

    private val String.isDeliusValidationMessage get() = DeliusValidationError.isKnownValidationMessage(this)
}
