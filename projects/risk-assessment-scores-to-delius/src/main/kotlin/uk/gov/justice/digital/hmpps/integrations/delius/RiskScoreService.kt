package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.FailureToUpdateRiskScores
import java.time.ZonedDateTime

@Service
class RiskScoreService(jdbcTemplate: JdbcTemplate) {
    private val updateRsrScoresProcedure = SimpleJdbcCall(jdbcTemplate)
        .withCatalogName("pkg_triggersupport")
        .withProcedureName("procRsrUpdateCas")

    fun updateRsrScores(
        crn: String,
        eventNumber: Int,
        assessmentDate: ZonedDateTime,
        rsrScore: Double,
        rsrBand: String,
        ospIndecentScore: Double,
        ospIndecentBand: String,
        ospContactScore: Double,
        ospContactBand: String,
    ) {
        val result = updateRsrScoresProcedure.execute(
            MapSqlParameterSource()
                .addValue("p_crn", crn)
                .addValue("p_event_number", eventNumber)
                .addValue("p_rsr_assessor_date", assessmentDate)
                .addValue("p_rsr_score", rsrScore)
                .addValue("p_rsr_band", rsrBand)
                .addValue("p_osp_indecent_score", ospIndecentScore)
                .addValue("p_osp_indecent_band", ospIndecentBand)
                .addValue("p_osp_contact_score", ospContactScore)
                .addValue("p_osp_contact_band", ospContactBand)
        )
        result["P_ERROR"]?.let { throw FailureToUpdateRiskScores(it as String) }
    }
}
