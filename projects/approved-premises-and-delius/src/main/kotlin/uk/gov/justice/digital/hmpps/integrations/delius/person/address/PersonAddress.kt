package uk.gov.justice.digital.hmpps.integrations.delius.person.address

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "offender_address")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "offender_address_id_generator", sequenceName = "offender_address_id_seq", allocationSize = 1)
class PersonAddress(
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean? = false,
    @Convert(converter = YesNoConverter::class)
    val typeVerified: Boolean? = false,
    val startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    @Column(columnDefinition = "number")
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
    val partitionAreaId: Long = 0,

    @Id
    @Column(name = "offender_address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offender_address_id_generator")
    val id: Long? = null,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,
)
