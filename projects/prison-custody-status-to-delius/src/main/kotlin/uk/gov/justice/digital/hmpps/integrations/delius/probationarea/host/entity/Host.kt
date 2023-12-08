package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    val endDate: ZonedDateTime? = null,
)

interface HostRepository : JpaRepository<Host, Long> {
    @Query(
        """
        select host.probationAreaId from Host host
        where host.leadHost = true 
        and host.institutionId = :institutionId
        and host.startDate <= :date
        and (host.endDate is null or host.endDate >= :date)
        """,
    )
    fun findLeadHostProviderIdByInstitutionId(
        institutionId: Long,
        date: ZonedDateTime,
    ): Long?
}
