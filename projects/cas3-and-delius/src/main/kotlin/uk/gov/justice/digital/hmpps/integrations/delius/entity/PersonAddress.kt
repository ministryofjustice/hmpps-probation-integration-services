package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "offender_address")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "offender_address_id_generator", sequenceName = "offender_address_id_seq", allocationSize = 1)
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offender_address_id_generator")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,
    var streetName: String?,
    @Column(name = "town_city")
    var town: String?,
    var county: String?,
    var postcode: String?,
    val telephoneNumber: String? = null,
    var buildingName: String? = null,
    var district: String? = null,
    val addressNumber: String? = null,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean? = false,
    @Convert(converter = YesNoConverter::class)
    val typeVerified: Boolean? = false,
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    @Column(updatable = false, columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false)
    val partitionAreaId: Long = 0
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @Query(
        """
        select pa from PersonAddress pa
        join fetch pa.status
        join fetch pa.type
        where pa.personId = :personId 
        and pa.softDeleted = false  
        and pa.endDate is null 
        and pa.status.code = 'M'
    """
    )
    fun findMainAddress(personId: Long): PersonAddress?
}
