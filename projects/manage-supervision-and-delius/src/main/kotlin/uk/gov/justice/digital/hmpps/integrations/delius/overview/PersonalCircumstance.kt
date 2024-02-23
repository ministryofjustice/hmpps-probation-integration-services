package uk.gov.justice.digital.hmpps.integrations.delius.overview

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "personal_circumstance")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "circumstance_type_id")
    val type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id")
    val subType: ReferenceData,

    val startDate: LocalDate,

    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,


) : Comparable<PersonalCircumstance> {
    override fun compareTo(other: PersonalCircumstance): Int {
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

