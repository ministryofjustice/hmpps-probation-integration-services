package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.staff.CommunityManager
import uk.gov.justice.digital.hmpps.model.Name
import java.time.LocalDate

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
    val firstName: String,
    val secondName: String? = null,
    val thirdName: String? = null,
    val surname: String,
    val preferredName: String? = null,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    val mobileNumber: String? = null,
    val telephoneNumber: String? = null,
    @Column(name = "e_mail_address")
    val emailAddress: String? = null,
    @OneToOne(mappedBy = "person")
    val manager: CommunityManager,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun name() = Name(firstName, secondName, thirdName, surname)
}
