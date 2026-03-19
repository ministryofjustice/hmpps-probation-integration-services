package uk.gov.justice.digital.hmpps.integrations.delius.person.contact

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalContactEntity(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val case: ProbationCase,

    @Column(name = "relationship")
    val relationship: String,

    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val relationshipType: ReferenceData,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "other_names")
    val middleName: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: AddressEntity?,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
