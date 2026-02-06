package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "responsible_officer")
@SequenceGenerator(
    name = "responsible_officer_id_generator",
    sequenceName = "responsible_officer_id_seq",
    allocationSize = 1
)
class ResponsibleOfficer(
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
    @Column(name = "row_version")
    var version: Long = 0,

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Id
    @GeneratedId(generator = "responsible_officer_id_generator")
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long? = null
)
