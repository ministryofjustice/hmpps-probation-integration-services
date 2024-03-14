package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "provision")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Provision(
    @Id
    @Column(name = "provision_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "provision_type_id")
    val type: ReferenceData,

    val startDate: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

interface ProvisionRepository : JpaRepository<Provision, Long> {
    fun findByPersonId(personId: Long): List<Provision>
}