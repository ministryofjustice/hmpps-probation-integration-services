package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host

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

    @Column(nullable = false)
    val probationAreaId: Long,

    @Column
    @Type(type = "yes_no")
    val leadHost: Boolean? = false,

    @Column(nullable = false)
    val startDate: ZonedDateTime,

    @Column
    val endDate: ZonedDateTime? = null,
)
