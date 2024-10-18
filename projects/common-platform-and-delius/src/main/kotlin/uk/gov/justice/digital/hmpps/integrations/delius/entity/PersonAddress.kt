package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
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
    val id: Long? = null,

    @Column(name = "start_date")
    val start: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "partition_area_id", nullable = false)
    val partitionAreaId: Long = 0,

    @Column(name = "soft_deleted", updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "row_version")
    @Version
    val rowVersion: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "no_fixed_abode")
    val noFixedAbode: Boolean? = false,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(name = "address_number")
    val addressNumber: String? = null,

    @Column(name = "street_name")
    var streetName: String? = null,

    @Column(name = "district")
    var district: String? = null,

    @Column(name = "town_city")
    var town: String? = null,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "county")
    var county: String? = null,

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "building_name")
    var buildingName: String? = null,

    @Column(name = "postcode")
    var postcode: String? = null,

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "telephone_number")
    val telephoneNumber: String? = null,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "awaiting_assessment", columnDefinition = "NUMBER", nullable = false)
    val awaitingAssessment: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData,

    @Column(name = "type_verified")
    @Convert(converter = YesNoConverter::class)
    val typeVerified: Boolean? = false,

    @Column(name = "approved_premises_residence_id")
    val approvedPremisesResidenceId: Long? = null,
    )


interface PersonAddressRepository : JpaRepository<PersonAddress, Long>