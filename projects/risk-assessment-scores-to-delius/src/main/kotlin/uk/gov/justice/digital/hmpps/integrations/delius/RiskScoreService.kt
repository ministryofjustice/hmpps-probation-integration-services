package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.listener.RiskAssessment
import java.time.ZonedDateTime

@Service
class RiskScoreService(jdbcTemplate: JdbcTemplate) {
    private val updateRsrScoresProcedure = SimpleJdbcCall(jdbcTemplate)
        .withCatalogName("pkg_triggersupport")
        .withProcedureName("procUpdateCAS")

    fun updateRsrScores(
        crn: String,
        eventNumber: Int,
        assessmentDate: ZonedDateTime,
        rsr: RiskAssessment,
        ospIndecent: RiskAssessment,
        ospContact: RiskAssessment,
    ) {
        updateRsrScoresProcedure.execute(
            MapSqlParameterSource()
                .addValue("p_crn", crn)
                .addValue("p_event_number", eventNumber)
                .addValue("p_rsr_assessor_date", assessmentDate)
                .addValue("p_rsr_score", rsr.score)
                .addValue("p_rsr_level_code", rsr.band)
                .addValue("p_osp_score_i", ospIndecent.score)
                .addValue("p_osp_level_i_code", ospIndecent.band)
                .addValue("p_osp_score_c", ospContact.score)
                .addValue("p_osp_level_c_code", ospContact.band)
        )
    }
}
