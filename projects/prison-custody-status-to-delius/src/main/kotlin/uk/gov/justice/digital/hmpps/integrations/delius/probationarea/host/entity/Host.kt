package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import java.time.ZonedDateTime

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
    @Convert(converter = YesNoConverter::class)
    val leadHost: Boolean? = false,

    @Column(nullable = false)
    val startDate: ZonedDateTime,

    @Column
    val endDate: ZonedDateTime? = null
)
