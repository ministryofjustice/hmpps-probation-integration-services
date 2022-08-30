package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
class ResponsibleOfficer(
    @Id
    @SequenceGenerator(name = "responsible_officer_id_generator", sequenceName = "responsible_officer_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responsible_officer_id_generator")
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager? = null,

    @ManyToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonManager: PrisonManager? = null,

    @Column
    val startDate: ZonedDateTime,

    @Column
    var endDate: ZonedDateTime? = null,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
) {
    fun stringDetails() = if (prisonManager != null) {
        """
            Responsible Officer Type: Prison Offender Manager
            Responsible Officer: ${prisonManager.staff.displayName()} (${prisonManager.team.description}, ${prisonManager.probationArea.description})
            Start Date: ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))}
            Allocation Reason: ${prisonManager.allocationReason.description}
        """.trimIndent()
    } else if (communityManager != null) {
        """
            Responsible Officer Type: Offender Manager
            Responsible Officer: ${communityManager.staff.displayName()} (${communityManager.team.description}, ${communityManager.probationArea.description})
            Start Date: ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))}
            Allocation Reason: ${communityManager.allocationReason.description}
        """.trimIndent()
    } else null
}
