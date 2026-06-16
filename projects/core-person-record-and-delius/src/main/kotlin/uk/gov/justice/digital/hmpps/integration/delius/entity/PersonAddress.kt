package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
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
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    @SequenceGenerator(name = "offender_address_id_seq", sequenceName = "offender_address_id_seq", allocationSize = 1)
    @GeneratedId(generator = "offender_address_id_seq")
    val id: Long? = null,
    @Version
    @Column(name = "row_version")
    val version: Long = 0,
    @Column(name = "offender_id")
    val personId: Long,
    var addressNumber: String?,
    var buildingName: String?,
    var streetName: String?,
    var townCity: String?,
    var county: String?,
    var district: String?,
    var postcode: String?,
    var uprn: Long?,
    var telephoneNumber: String?,
    @Convert(converter = YesNoConverter::class)
    var noFixedAbode: Boolean,
    @Lob
    var notes: String?,
    var startDate: ZonedDateTime?,
    var endDate: ZonedDateTime?,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "address_type_id")
    var type: ReferenceData?,
    @Convert(converter = YesNoConverter::class)
    var typeVerified: Boolean?,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false,
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    @CreatedBy
    var createdByUserId: Long = 0,
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    val partitionAreaId: Long = 0
)

interface AddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status", "type"])
    fun findAllByPersonIdOrderByStartDateDesc(personId: Long): List<PersonAddress>

    @EntityGraph(attributePaths = ["status", "type"])
    fun findAllByPersonIdInOrderByStartDateDesc(personId: Collection<Long>): List<PersonAddress>
}