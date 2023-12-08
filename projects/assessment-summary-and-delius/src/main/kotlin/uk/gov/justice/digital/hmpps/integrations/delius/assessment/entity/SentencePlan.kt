package uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.io.Serializable

@Entity
@Table(name = "oasys_sentence_plan")
@SequenceGenerator(name = "oasys_sentence_plan_id_seq", sequenceName = "oasys_sentence_plan_id_seq", allocationSize = 1)
class SentencePlan(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "oasys_assessment_id")
    val assessment: OasysAssessment,

    var objectiveNumber: Long,
    val objective: String,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @GeneratedValue(generator = "oasys_sentence_plan_id_seq")
    @Column(name = "oasys_sentence_plan_id")
    val id: Long
) {
    @OneToMany(mappedBy = "sentencePlan", cascade = [CascadeType.ALL])
    var needs: List<Need> = listOf()
        private set

    @OneToMany(mappedBy = "sentencePlan", cascade = [CascadeType.ALL])
    var texts: List<Text> = listOf()
        private set

    @OneToMany(mappedBy = "sentencePlan", cascade = [CascadeType.ALL])
    var workSummaries: List<WorkSummary> = listOf()
        private set

    fun withNeed(level: Long, need: String): SentencePlan {
        needs = needs + Need(SentencePlanRelatedId(this, level), need, this, person)
        return this
    }

    fun withText(level: Long, text: String): SentencePlan {
        texts = texts + Text(SentencePlanRelatedId(this, level), text, this, person)
        return this
    }

    fun withWorkSummary(level: Long, workSummary: String): SentencePlan {
        workSummaries = workSummaries + WorkSummary(SentencePlanRelatedId(this, level), workSummary, this, person)
        return this
    }
}

@Entity
@Table(name = "oasys_sp_need")
class Need(
    @EmbeddedId
    val id: SentencePlanRelatedId,

    val need: String,

    @ManyToOne
    @JoinColumn(name = "oasys_sentence_plan_id")
    val sentencePlan: SentencePlan,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Entity
@Table(name = "oasys_sp_text")
class Text(
    @EmbeddedId
    val id: SentencePlanRelatedId,

    val text: String,

    @ManyToOne
    @JoinColumn(name = "oasys_sentence_plan_id")
    val sentencePlan: SentencePlan,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Entity
@Table(name = "oasys_sp_work_summary")
class WorkSummary(
    @EmbeddedId
    val id: SentencePlanRelatedId,

    val workSummary: String,

    @ManyToOne
    @JoinColumn(name = "oasys_sentence_plan_id")
    val sentencePlan: SentencePlan,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Embeddable
class SentencePlanRelatedId(
    @ManyToOne
    @JoinColumn(name = "oasys_sentence_plan_id")
    val sentencePlan: SentencePlan,

    @Column(name = "level_")
    val level: Long
) : Serializable
