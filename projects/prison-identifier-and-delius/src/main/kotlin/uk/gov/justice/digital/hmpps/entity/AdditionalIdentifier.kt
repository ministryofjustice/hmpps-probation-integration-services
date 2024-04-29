package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(
    name = "additional_identifier_id_seq",
    sequenceName = "additional_identifier_id_seq",
    allocationSize = 1
)
class AdditionalIdentifier(

    @Column(columnDefinition = "varchar2(30)")
    val identifier: String,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val type: ReferenceData,

    @Column(name = "offender_id")
    val personId: Long,

    @Column
    val partitionAreaId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "additional_identifier_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "additional_identifier_id_seq")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

interface AdditionalIdentifierRepository : JpaRepository<AdditionalIdentifier, Long>
