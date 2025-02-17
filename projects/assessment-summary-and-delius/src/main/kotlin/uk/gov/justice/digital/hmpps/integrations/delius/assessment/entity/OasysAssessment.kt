package uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity

import jakarta.persistence.*
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.oasys.WeightedScores
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "oasys_assessment")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "oasys_assessment_id_seq", sequenceName = "oasys_assessment_id_seq", allocationSize = 1)
class OasysAssessment(
    @Column(name = "oasys_id")
    val oasysId: String,

    @Column(name = "assessment_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val eventNumber: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "contact_id")
    @NotFound(action = NotFoundAction.IGNORE) // No foreign key on contact_id
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court?,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence?,

    @Column(name = "oasys_total_score")
    val totalScore: Long?,

    @Column(name = "oasys_assessment_description")
    val description: String?,
    val assessedBy: String?,
    val riskFlags: String?,
    val concernFlags: String?,

    val dateCreated: LocalDate,
    @Column(name = "assessment_received_date")
    val dateReceived: LocalDate,
    val initialSentencePlanDate: LocalDate?,
    val sentencePlanReviewDate: LocalDate?,

    @Convert(converter = YesNoConverter::class)
    val reviewTerminated: Boolean?,
    val reviewNumber: String?,
    val layerType: String?,

    @Column(name = "ogrs_score_1")
    val ogrsScore1: Long?,
    @Column(name = "ogrs_score_2")
    val ogrsScore2: Long?,
    @Column(name = "ogp_score_1")
    val ogpScore1: Long?,
    @Column(name = "ogp_score_2")
    val ogpScore2: Long?,
    @Column(name = "ovp_score_1")
    val ovpScore1: Long?,
    @Column(name = "ovp_score_2")
    val ovpScore2: Long?,

    @ManyToOne
    @JoinColumn(name = "assessment_status_id")
    val status: ReferenceData?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @GeneratedValue(generator = "oasys_assessment_id_seq")
    @Column(name = "oasys_assessment_id")
    val id: Long = 0
) : AuditableEntity() {

    init {
        check(riskFlags?.matches(RISK_FLAGS_PATTERN) ?: true) {
            "Risk Flags received not matching expected format: $riskFlags"
        }
        check(concernFlags?.matches(CONCERN_FLAGS_PATTERN) ?: true) {
            "Concern Flags received not matching expected format: $concernFlags"
        }
    }

    @OneToMany(mappedBy = "id.assessment", cascade = [CascadeType.ALL])
    var sectionScores: List<SectionScore> = listOf()
        private set

    @OneToMany(mappedBy = "assessment", cascade = [CascadeType.ALL])
    var sentencePlans: List<SentencePlan> = listOf()
        private set

    fun withSectionScores(weightedScores: WeightedScores): OasysAssessment {
        sectionScores = weightedScores.asSectionScores().map { SectionScore(SectionScoreId(this, it.first), it.second) }
        return this
    }

    fun withSentencePlan(sentencePlan: SentencePlan): OasysAssessment {
        sentencePlans = sentencePlans + sentencePlan
        return this
    }

    companion object {
        val RISK_FLAGS_PATTERN: Regex = "^([VvHhMmLlNn],){8}[VvHhMmLlNn]$".toRegex()
        val CONCERN_FLAGS_PATTERN: Regex = "^((NO|YES|DK),){7}(NO|YES|DK)$".toRegex()
    }

    private fun WeightedScores.asSectionScores(): List<Pair<Long, Long>> = listOfNotNull(
        accommodationWeightedScore?.let { 3L to it },
        eteWeightedScore?.let { 4L to it },
        relationshipsWeightedScore?.let { 6L to it },
        lifestyleWeightedScore?.let { 7L to it },
        drugWeightedScore?.let { 8L to it },
        alcoholWeightedScore?.let { 9L to it },
        thinkingWeightedScore?.let { 11L to it },
        attitudesWeightedScore?.let { 12L to it },
    )
}

@Entity
@Table(name = "oasys_assmnt_section_score")
class SectionScore(

    @EmbeddedId
    val id: SectionScoreId,

    @Column(name = "section_score")
    var score: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Embeddable
class SectionScoreId(
    @ManyToOne
    @JoinColumn(name = "oasys_assessment_id")
    val assessment: OasysAssessment,

    @Column(name = "level_")
    val level: Long
) : Serializable

interface OasysAssessmentRepository : JpaRepository<OasysAssessment, Long> {
    @EntityGraph(attributePaths = ["sectionScores"])
    fun findByOasysId(oasysId: String): OasysAssessment?
}
