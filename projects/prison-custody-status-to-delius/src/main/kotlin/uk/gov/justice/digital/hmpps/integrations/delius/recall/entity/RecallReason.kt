package uk.gov.justice.digital.hmpps.integrations.delius.recall.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

@Immutable
@Entity
@Table(name = "r_recall_reason")
class RecallReason(
    @Id
    @Column(name = "recall_reason_id")
    val id: Long,
    @Column(nullable = false)
    val code: String,
    @Column(nullable = false)
    val description: String,
    @ManyToOne
    @JoinColumn(name = "termination_reason_id")
    val licenceConditionTerminationReason: ReferenceData,
    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,
) {
    enum class Code(val value: String) {
        NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT("NN"),
        END_OF_TEMPORARY_LICENCE("EOTL"),
        TRANSFER_TO_SECURE_HOSPITAL("TSH"),
        TRANSFER_TO_IRC("IRC"),
    }
}

fun RecallReason?.isEotl() = this != null && RecallReason.Code.END_OF_TEMPORARY_LICENCE.value == code

interface RecallReasonRepository : JpaRepository<RecallReason, Long> {
    fun findByCode(code: String): RecallReason?
}

fun RecallReasonRepository.getByCode(code: String): RecallReason =
    findByCode(code) ?: throw NotFoundException("RecallReason", "code", code)
