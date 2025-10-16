package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.ProbationCase

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name")
    val forename: String,

    @Column
    val secondName: String?,

    @Column
    val thirdName: String?,

    @Column
    val surname: String,

    @Column
    val mobileNumber: String?,

    @OneToOne(mappedBy = "person")
    val manager: ManagerEntity?,

    @OneToMany(mappedBy = "person")
    val events: List<Event>,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun toProbationCase(getEmail: (String) -> String?) = ProbationCase(
        name = listOfNotNull(forename, secondName, thirdName, surname).joinToString(" "),
        crn = crn,
        mobileNumber = mobileNumber,
        manager = checkNotNull(manager).staff.let { staff ->
            Manager(
                name = listOfNotNull(staff.forename, staff.surname).joinToString(" "),
                email = staff.user?.username?.let { getEmail(it) }
            )
        },
        probationDeliveryUnit = manager.team.localAdminUnit.probationDeliveryUnit.description
    )
}