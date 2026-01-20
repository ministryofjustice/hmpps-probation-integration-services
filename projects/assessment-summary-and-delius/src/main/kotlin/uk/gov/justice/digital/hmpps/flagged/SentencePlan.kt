package uk.gov.justice.digital.hmpps.flagged

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.io.Serializable

@Entity
@Table(name = "oasys_sentence_plan")
@SequenceGenerator(name = "oasys_sentence_plan_id_seq", sequenceName = "oasys_sentence_plan_id_seq", allocationSize = 1)
class FlaggedSentencePlan(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "oasys_assessment_id")
    val assessment: FlaggedOasysAssessment,

    val objectiveNumber: Long,
    val objective: String,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @GeneratedId(generator = "oasys_sentence_plan_id_seq")
    @Column(name = "oasys_sentence_plan_id")
    val id: Long? = null
) {
    val partitionAreaId: Long = 0

    @OneToMany(mappedBy = "id.sentencePlan", cascade = [CascadeType.ALL])
    var needs: List<FlaggedNeed> = listOf()
        private set

    @OneToMany(mappedBy = "id.sentencePlan", cascade = [CascadeType.ALL])
    var texts: List<FlaggedText> = listOf()
        private set

    @OneToMany(mappedBy = "id.sentencePlan", cascade = [CascadeType.ALL])
    var workSummaries: List<FlaggedWorkSummary> = listOf()
        private set

    fun withNeed(level: Long, need: String): FlaggedSentencePlan {
        needs = needs + FlaggedNeed(FlaggedSentencePlanRelatedId(this, level), need, person)
        return this
    }

    fun withText(level: Long, text: String): FlaggedSentencePlan {
        texts = texts + FlaggedText(FlaggedSentencePlanRelatedId(this, level), text, person)
        return this
    }

    fun withWorkSummary(level: Long, workSummary: String): FlaggedSentencePlan {
        workSummaries =
            workSummaries + FlaggedWorkSummary(FlaggedSentencePlanRelatedId(this, level), workSummary, person)
        return this
    }
}

@Entity
@Table(name = "oasys_sp_need")
class FlaggedNeed(
    @EmbeddedId
    val id: FlaggedSentencePlanRelatedId,

    val need: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Entity
@Table(name = "oasys_sp_text")
class FlaggedText(
    @EmbeddedId
    val id: FlaggedSentencePlanRelatedId,

    val text: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    val partitionAreaId: Long = 0
}

@Entity
@Table(name = "oasys_sp_work_summary")
class FlaggedWorkSummary(
    @EmbeddedId
    val id: FlaggedSentencePlanRelatedId,

    val workSummary: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

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
class FlaggedSentencePlanRelatedId(
    @ManyToOne
    @JoinColumn(name = "oasys_sentence_plan_id")
    val sentencePlan: FlaggedSentencePlan,

    @Column(name = "level_")
    val level: Long
) : Serializable
