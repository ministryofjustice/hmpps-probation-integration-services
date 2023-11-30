package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: AddressStatus,

    @Column(name = "building_name")
    val buildingName: String?,
    @Column(name = "address_number")
    val buildingNumber: String?,
    @Column(name = "street_name")
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,

    val startDate: LocalDate,
    val endDate: LocalDate?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class AddressStatus(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    fun findAllByPersonCrnOrderByStartDateDesc(crn: String): List<PersonAddress>
}
