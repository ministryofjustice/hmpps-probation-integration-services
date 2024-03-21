package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonAddress(
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,
    val postcode: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val softDeleted: Boolean,
    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

interface AddressRepository : JpaRepository<PersonAddress, Long> {
    fun findAllByPersonIdAndStatusCodeInOrderByStartDateDesc(
        personId: Long,
        statusCodes: List<String>
    ): List<PersonAddress>
}

fun AddressRepository.mainAddresses(personId: Long) =
    findAllByPersonIdAndStatusCodeInOrderByStartDateDesc(personId, listOf("M"))