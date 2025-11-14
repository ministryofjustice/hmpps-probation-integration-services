package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "enforcement")
class Enforcement(
    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val enforcementAction: EnforcementAction? = null,

    @Column(name = "response_date")
    val responseDate: LocalDate? = null,

    )

interface EnforcementRepository : JpaRepository<Enforcement, Long>