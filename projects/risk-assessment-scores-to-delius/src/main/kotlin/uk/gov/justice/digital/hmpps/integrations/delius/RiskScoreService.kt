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
class RiskScoreService(
    jdbcTemplate: JdbcTemplate,
    private val featureFlags: FeatureFlags
) {
    private val updateRsrAndOspScoresProcedureV4: SimpleJdbcCall =
        SimpleJdbcCall(jdbcTemplate)
            .withCatalogName("pkg_triggersupport")
            .withProcedureName("procUpdateCAS")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(*sqlParametersFor(true))

    private val updateRsrAndOspScoresProcedureV3: SimpleJdbcCall =
        SimpleJdbcCall(jdbcTemplate)
            .withCatalogName("pkg_triggersupport")
            .withProcedureName("procUpdateCAS")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(*sqlParametersFor(false))

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
            val supportsOgrs4 = featureFlags.enabled("delius-ogrs4-support")
            val params = baseParams(
                crn = crn,
                eventNumber = eventNumber,
                assessmentDate = assessmentDate,
                rsr = rsr,
                ospIndecent = ospIndecent,
                ospContact = ospContact,
                ospIndirectIndecent = ospIndirectIndecent,
                ospDirectContact = ospDirectContact
            ).apply {
                if (supportsOgrs4) addOgrs4Params(
                    rsr = rsr,
                    ospIndecent = ospIndecent,
                    ospContact = ospContact,
                    ospIndirectIndecent = ospIndirectIndecent,
                    ospDirectContact = ospDirectContact
                )
            }
            if (supportsOgrs4) updateRsrAndOspScoresProcedureV4.execute(params)
            else updateRsrAndOspScoresProcedureV3.execute(params)
        } catch (e: UncategorizedSQLException) {
            e.sqlException?.takeIf { it.isValidationError }
                ?.parsedValidationMessage
                ?.takeIf { it.isDeliusValidationMessage }
                ?.let { throw DeliusValidationError(it) }
            throw e
        }
    }

    private fun sqlParametersFor(ogrs4: Boolean): Array<SqlParameter> {
        val base = listOf(
            SqlParameter("p_crn", Types.VARCHAR),
            SqlParameter("p_event_number", Types.NUMERIC),
            SqlParameter("p_rsr_assessor_date", Types.DATE),
            SqlParameter("p_rsr_score", Types.NUMERIC),
            SqlParameter("p_rsr_level_code", Types.VARCHAR),
            SqlParameter("p_osp_score_i", Types.NUMERIC),
            SqlParameter("p_osp_score_c", Types.NUMERIC),
            SqlParameter("p_osp_level_i_code", Types.VARCHAR),
            SqlParameter("p_osp_level_c_code", Types.VARCHAR),
            SqlParameter("p_osp_level_iic_code", Types.VARCHAR),
            SqlParameter("p_osp_level_dc_code", Types.VARCHAR),
            SqlParameter("p_rsr_static_flag", Types.VARCHAR),
        )

        val ogrs4Extras = listOf(
            SqlParameter("p_rsr_version", Types.NUMERIC),
            SqlParameter("p_osp_version_i", Types.NUMERIC),
            SqlParameter("p_osp_version_c", Types.NUMERIC),
            SqlParameter("p_osp_score_dc", Types.NUMERIC),
            SqlParameter("p_osp_score_iic", Types.NUMERIC),
        )

        return (if (ogrs4) base + ogrs4Extras else base).toTypedArray()
    }

    private fun baseParams(
        crn: String,
        eventNumber: Int?,
        assessmentDate: ZonedDateTime,
        rsr: RiskAssessment,
        ospIndecent: RiskAssessment?,
        ospContact: RiskAssessment?,
        ospIndirectIndecent: RiskAssessment?,
        ospDirectContact: RiskAssessment?,
    ): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("p_crn", crn)
            .addValue("p_event_number", eventNumber)
            .addValue("p_rsr_assessor_date", assessmentDate)
            .addValue("p_rsr_score", rsr.score)
            .addValue("p_rsr_level_code", rsr.band)
            .addValue("p_osp_score_i", ospIndecent?.score)
            .addValue("p_osp_score_c", ospContact?.score)
            .addValue("p_osp_level_i_code", ospIndecent?.band)
            .addValue("p_osp_level_c_code", ospContact?.band)
            .addValue("p_osp_level_iic_code", ospIndirectIndecent?.band)
            .addValue("p_osp_level_dc_code", ospDirectContact?.band)
            .addValue("p_rsr_static_flag", rsr.staticOrDynamic)

    private fun MapSqlParameterSource.addOgrs4Params(
        rsr: RiskAssessment,
        ospIndecent: RiskAssessment?,
        ospContact: RiskAssessment?,
        ospIndirectIndecent: RiskAssessment?,
        ospDirectContact: RiskAssessment?,
    ): MapSqlParameterSource = this
        .addValue("p_rsr_version", rsr.versionIfV4())
        .addValue("p_osp_version_i", ospIndecent.versionIfV4())
        .addValue("p_osp_version_c", ospContact.versionIfV4())
        .addValue("p_osp_score_dc", ospDirectContact?.score)
        .addValue("p_osp_score_iic", ospIndirectIndecent?.score)

    // Only V4 has an algorithmVersion; V3 (and null) should yield null
    private fun RiskAssessment?.versionIfV4(): Int? =
        (this as? RiskAssessment.V4)?.algorithmVersion

    private val SQLException.isValidationError get() = errorCode == 20000
    private val SQLException.parsedValidationMessage
        get() = message
            ?.replace(Regex("\\n.*"), "") // take the first line
            ?.replace(Regex("\\[[^]]++]\\s*"), "") // remove anything inside square brackets
            ?.removePrefix("ORA-20000: INTERNAL ERROR: An unexpected error in PL/SQL: ERROR : ")
            ?.trim()

    private val String.isDeliusValidationMessage
        get() = DeliusValidationError.isKnownValidationMessage(this)
}
