package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate

@Immutable
@Entity(name = "PersonalDetailsAddress")
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData?,

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
    val telephoneNumber: String?,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,

    @Convert(converter = YesNoConverter::class)
    val typeVerified: Boolean? = false,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Lob
    @Column
    val notes: String?,

    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status", "type"])
    fun findByPersonId(personId: Long): List<PersonAddress>
}

@Immutable
@Entity
@Table(name = "address")
@SQLRestriction("soft_deleted = 0")
class ContactAddress(
    @Id
    @Column(name = "address_id")
    val id: Long,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)