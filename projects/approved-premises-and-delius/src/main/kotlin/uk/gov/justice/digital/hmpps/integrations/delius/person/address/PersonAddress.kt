package uk.gov.justice.digital.hmpps.integrations.delius.person.address

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
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
    @Column(updatable = false, columnDefinition = "NUMBER")
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
)
