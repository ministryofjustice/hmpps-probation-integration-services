package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "rqmnt")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    var person: Person,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    var active: Boolean,
)
