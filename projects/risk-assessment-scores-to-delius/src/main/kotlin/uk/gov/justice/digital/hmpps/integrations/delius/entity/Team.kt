package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
)

interface TeamRepository : JpaRepository<Team, Long>
