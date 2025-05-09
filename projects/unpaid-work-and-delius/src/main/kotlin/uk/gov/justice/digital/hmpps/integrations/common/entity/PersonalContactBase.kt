package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false

)
