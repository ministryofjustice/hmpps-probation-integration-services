package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,
    val addressNumber: String?,
    var buildingName: String?,
    var streetName: String?,
    var townCity: String?,
    var county: String?,
    var district: String?,
    val postcode: String?,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

interface AddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status"])
    fun findAllByPersonIdOrderByStartDateDesc(
        personId: Long
    ): List<PersonAddress>
}