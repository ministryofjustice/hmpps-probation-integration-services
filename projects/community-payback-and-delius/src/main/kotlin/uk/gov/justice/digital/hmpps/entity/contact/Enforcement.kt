package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import java.time.LocalDate

@Entity
@Immutable
class Enforcement(
    @Id
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @OneToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: EnforcementAction,

    @Column(name = "response_date")
    val responseDate: LocalDate
)
