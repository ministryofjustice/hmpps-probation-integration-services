package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonAddress(

    @Id
    @Column(name = "offender_address_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData?,

    val buildingName: String?,

    @Column(name = "address_number")
    val buildingNumber: String?,

    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val noFixedAbode: Boolean?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    fun findByPersonId(personId: Long): List<PersonAddress>
}
