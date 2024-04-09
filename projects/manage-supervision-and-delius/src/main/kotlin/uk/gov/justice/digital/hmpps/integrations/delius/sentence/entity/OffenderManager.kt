package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import java.time.LocalDate

@Entity
@Immutable
class OffenderManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val allocationStaffId: Long?,

    val probationAreaId: Long,

    val endDate: LocalDate?
)