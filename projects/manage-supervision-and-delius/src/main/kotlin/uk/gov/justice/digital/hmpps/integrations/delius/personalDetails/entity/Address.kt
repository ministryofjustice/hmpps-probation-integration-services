package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate

@Entity(name = "PersonalDetailsAddress")
@Table(name = "offender_address")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "offender_address_id_generator", sequenceName = "offender_address_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class PersonAddress(

    @Column(name = "offender_id")
    val personId: Long,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    var type: ReferenceData?,

    @Column(name = "building_name")
    var buildingName: String?,
    @Column(name = "address_number")
    var buildingNumber: String?,
    @Column(name = "street_name")
    var streetName: String?,
    var district: String? = null,
    @Column(name = "town_city")
    var town: String?,
    var county: String?,
    var postcode: String?,
    val telephoneNumber: String? = null,
    var startDate: LocalDate,
    var endDate: LocalDate? = null,

    @Convert(converter = YesNoConverter::class)
    var typeVerified: Boolean? = false,

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    var noFixedAbode: Boolean = false,

    @Column(name = "last_updated_datetime")
    @LastModifiedDate
    var lastUpdated: LocalDate? = LocalDate.now(),

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @ManyToOne
    @JoinColumns(
        JoinColumn(
            name = "last_updated_user_id",
            referencedColumnName = "user_id",
            insertable = false,
            updatable = false
        )
    )
    val lastUpdatedUser: User? = null,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Lob
    @Column
    var notes: String?,

    @Column(nullable = false)
    val partitionAreaId: Long = 0,

    @Id
    @Column(name = "offender_address_id")
    @GeneratedId(generator = "offender_address_id_generator")
    val id: Long? = null,
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

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
