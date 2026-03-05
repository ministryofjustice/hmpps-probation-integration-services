package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.model.Name
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and (start_date is null or start_date <= current_date) and (end_date is null or end_date > current_date)")
class PersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val type: ReferenceData,
    val firstName: String,
    val surname: String,
    val relationship: String,
    val mobileNumber: String?,
    val emailAddress: String?,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun name() = Name(firstName, null, surname)

    companion object {
        const val EMERGENCY_CONTACT = "ME"
    }
}
