package uk.gov.justice.digital.hmpps.integrations.delius.probationarea

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "r_host")
class Host(
    @Id
    @Column(name = "host_id")
    val id: Long,

    @Column
    val institutionId: Long,

    @Column(name = "probation_area_id", nullable = false)
    val providerId: Long,

    @Column(name = "lead_host")
    @Type(type = "yes_no")
    val leadHost: Boolean? = false,

    @Column(name = "start_date", nullable = false)
    val startDate: ZonedDateTime,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,
)
