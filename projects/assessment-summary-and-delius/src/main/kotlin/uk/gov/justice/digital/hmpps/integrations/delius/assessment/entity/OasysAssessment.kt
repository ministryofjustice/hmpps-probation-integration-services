package uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
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

    @OneToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court?,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence?,

    @ManyToOne
    @JoinColumn(name = "tier_id")
    val tier: ReferenceData?,

    @Column(name = "oasys_total_score")
    val totalScore: Long,

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

    val ogrsScore1: Long?,
    val ogrsScore2: Long?,
    val ogpScore1: Long?,
    val ogpScore2: Long?,
    val ovpScore1: Long?,
    val ovpScore2: Long?,

    val objectiveStatus: String?,
    val layer1Objective: String?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

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

    @OneToMany(mappedBy = "assessment", cascade = [CascadeType.ALL])
    var sectionScores: List<SectionScore> = listOf()
        private set

    @OneToMany(mappedBy = "assessment", cascade = [CascadeType.ALL])
    var sentencePlans: List<SentencePlan> = listOf()
        private set

    fun withSectionScore(level: Long, score: Long): OasysAssessment {
        sectionScores = sectionScores + SectionScore(SectionScoreId(this, level), score, this)
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
}

@Entity
@Table(name = "oasys_assmnt_section_score")
class SectionScore(

    @EmbeddedId
    val id: SectionScoreId,

    @Column(name = "section_score")
    var score: Long,

    @ManyToOne
    @JoinColumn(name = "oasys_assessment_id")
    val assessment: OasysAssessment,

    @Column(columnDefinition = "number")
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
    fun findByOasysId(oasysId: String): OasysAssessment?
}
