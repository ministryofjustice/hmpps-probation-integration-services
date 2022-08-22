package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recall.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "soft_deleted = 0")
data class Release(
    @Id
    @SequenceGenerator(name = "release_id_generator", sequenceName = "release_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "release_id_generator")
    @Column(name = "release_id", nullable = false)
    val id: Long = 0,

    @Column(name = "row_version", nullable = false)
    @Version
    val version: Long = 0,

    @Column(name = "actual_release_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "release_type_id", nullable = false)
    val type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "custody_id", nullable = false)
    val custody: Custody? = null,

    @Embedded
    val institutionId: InstitutionId? = null,

    @Column(name = "probation_area_id", nullable = true)
    val leadHostProviderId: Long? = null,

    @OneToOne(mappedBy = "release")
    val recall: Recall? = null,

    @Column(name = "partition_area_id", nullable = false)
    val partitionAreaId: Long = 0,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_user_id", nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "created_datetime", nullable = false, updatable = false)
    @CreatedDate
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_datetime", nullable = false)
    @LastModifiedDate
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),
)
