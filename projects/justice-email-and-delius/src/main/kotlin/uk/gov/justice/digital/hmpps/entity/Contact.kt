package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(
    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    @Column
    val externalReference: String,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Lob
    val notes: String,

    val description: String?,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column
    val teamId: Long,

    @Column
    val staffId: Long,

    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column
    val partitionAreaId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String
) {
    enum class Code(val code: String) {
        EMAIL("C135")
    }
}

interface ContactRepository : JpaRepository<Contact, Long>

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(type: ContactType.Code) = findByCode(type.code)
    ?: throw NotFoundException("Contact Type", "code", type.code)
