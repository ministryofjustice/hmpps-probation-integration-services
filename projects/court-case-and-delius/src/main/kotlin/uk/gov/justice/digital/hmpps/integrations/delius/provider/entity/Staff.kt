package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column
    val forename2: String?,

    @Column
    val surname: String,

    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData?,

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun isUnallocated(): Boolean {
        return code.endsWith("U")
    }

    fun getName(): String {
        return when {
            forename2 == null -> "$forename $surname"
            else -> "$forename $forename2 $surname"
        }
    }
}

@Immutable
@Table(name = "team")
@Entity
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column
    val description: String,

    @Column
    val telephone: String? = null,

    @Column
    val emailAddress: String? = null,

    @ManyToOne
    @JoinColumn(name = "local_delivery_unit_id")
    val ldu: LocalDeliveryUnit,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District
)
