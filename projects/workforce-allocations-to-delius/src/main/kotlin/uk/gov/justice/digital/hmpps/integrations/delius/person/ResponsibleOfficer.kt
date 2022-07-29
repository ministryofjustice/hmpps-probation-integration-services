package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "responsible_officer")
@SequenceGenerator(
    name = "responsible_officer_id_generator",
    sequenceName = "responsible_officer_id_seq",
    allocationSize = 1
)
class ResponsibleOfficer(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responsible_officer_id_generator")
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager?,

    @ManyToOne
    @JoinColumn(name = "PRISON_OFFENDER_MANAGER_ID")
    var prisonManager: PrisonManager? = null,

    val startDate: ZonedDateTime,
    var endDate: ZonedDateTime? = null,

    @Version
    var version: Long = 0,

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    var createdDateTime: ZonedDateTime? = null,

    @LastModifiedDate
    var lastUpdatedDateTime: ZonedDateTime? = null,
)
