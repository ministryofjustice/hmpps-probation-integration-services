package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Immutable
import java.time.LocalDate

@MappedSuperclass
@Immutable
open class PersonalContactBase(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long = 0,
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
    val start: LocalDate? = null,
    @Column(name = "end_date")
    val endDate: LocalDate? = null,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)
