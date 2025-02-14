package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData?,

    val buildingName: String?,
    @Column(name = "address_number")
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_address_id")
    val id: Long,
)