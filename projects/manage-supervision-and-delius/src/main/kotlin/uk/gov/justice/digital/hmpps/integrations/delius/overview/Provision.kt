package uk.gov.justice.digital.hmpps.integrations.delius.overview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "provision")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Provision(
    @Id
    @Column(name = "provision_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "disability_type_id")
    val type: ReferenceData,

    val startDate: LocalDate,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,


) : Comparable<Provision> {
    override fun compareTo(other: Provision): Int {
        val startDate = -startDate.compareTo(other.startDate)

        if(startDate == 0){
            return -id.compareTo(other.id)
        }
        return startDate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Provision

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}