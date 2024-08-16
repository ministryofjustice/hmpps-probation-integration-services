package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.controller.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.math.BigDecimal
import java.time.LocalDateTime

interface OrdsClient {
    @GetExchange("/ass/allasslist/pris/{nomsId}/ALLOW")
    fun getTimeline(@PathVariable nomsId: String): Timeline

    @GetExchange("/ass/{name}/ALLOW/{id}")
    fun getSection(@PathVariable id: Long, @PathVariable name: String): ObjectNode

    @GetExchange("/ass/allrisk/{crn}/ALLOW")
    fun getSection(@PathVariable crn: String): OasysRiskPredictors
}

fun OrdsClient.getRiskPredictors(crn: String, assessmentId: Long): RiskPrediction =
    getSection(crn).assessments.firstOrNull { it.assessmentPk == assessmentId }
        ?.let {
            RiskPrediction(
                it.dateCompleted,
                it.assessmentStatus,
                with(it.ogr) {
                    YearPredictor.of(ogrs31Year, ogrs32Year, ogrs3RiskRecon)
                },
                with(it.ovp) {
                    YearPredictor.of(ovp1Year, ovp2Year, ovpRisk)
                },
                with(it.ogp) {
                    YearPredictor.of(ogp1Year, ogp2Year, ogpRisk)
                },
                with(it.rsr) {
                    RsrPredictor.of(scoreLevel, rsrPercentageScore)
                },
                with(it.osp) {
                    SexualPredictor.of(
                        ospImagePercentageScore,
                        ospContactPercentageScore,
                        ospImageScoreLevel,
                        ospContactScoreLevel,
                        ospIndirectImagesChildrenPercentageScore,
                        ospDirectContactPercentageScore,
                        ospIndirectImagesChildrenScoreLevel,
                        ospDirectContactScoreLevel
                    )
                }
            )
        } ?: throw NotFoundException("Assessment $assessmentId not found for $crn")

data class OasysRiskPredictors(
    val assessments: List<AssessmentRisk>
)

data class AssessmentRisk(
    val assessmentPk: Long,
    val dateCompleted: LocalDateTime,
    val assessmentType: String,
    val assessmentStatus: String,
    @JsonAlias("OGP")
    val ogp: OgpScore,
    @JsonAlias("OVP")
    val ovp: OvpScore,
    @JsonAlias("OGRS")
    val ogr: OgrScore,
    @JsonAlias("RSR")
    val rsr: RsrScore,
    @JsonAlias("OSP")
    val osp: OspScore,
)

data class OgpScore(
    val ogp1Year: BigDecimal? = null,
    val ogp2Year: BigDecimal? = null,
    val ogpRisk: String? = null,
)

data class OvpScore(
    val ovp1Year: BigDecimal? = null,
    val ovp2Year: BigDecimal? = null,
    val ovpRisk: String? = null,
)

data class OgrScore(
    val ogrs31Year: BigDecimal? = null,
    val ogrs32Year: BigDecimal? = null,
    val ogrs3RiskRecon: String? = null,
)

data class RsrScore(
    val rsrPercentageScore: BigDecimal? = null,
    val scoreLevel: String? = null,
)

data class OspScore(
    val ospImagePercentageScore: BigDecimal? = null,
    val ospContactPercentageScore: BigDecimal? = null,
    val ospImageScoreLevel: String? = null,
    val ospContactScoreLevel: String? = null,
    val ospIndirectImagesChildrenPercentageScore: BigDecimal? = null,
    val ospDirectContactPercentageScore: BigDecimal? = null,
    val ospIndirectImagesChildrenScoreLevel: String? = null,
    val ospDirectContactScoreLevel: String? = null
)