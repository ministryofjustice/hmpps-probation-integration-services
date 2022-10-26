package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.LocalAdminUnit
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val localAdminUnit: LocalAdminUnit,
)
