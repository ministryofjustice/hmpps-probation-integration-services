package uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.user.User
import java.time.LocalDateTime

@Immutable
@Entity
class Exclusion(
    @Id
    @Column(name = "exclusion_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "exclusion_end_time")
    val end: LocalDateTime? = null
)

@Immutable
@Entity
class Restriction(
    @Id
    @Column(name = "restriction_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "restriction_end_time")
    val end: LocalDateTime? = null
)
