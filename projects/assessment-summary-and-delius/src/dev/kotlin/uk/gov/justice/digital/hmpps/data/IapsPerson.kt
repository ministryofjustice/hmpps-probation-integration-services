package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "iaps_offender")
class IapsPerson(
    @Id
    @Column(name = "offender_id")
    val personId: Long,

    @Column(columnDefinition = "number")
    val iapsFlag: Boolean
)

interface IapsPersonRepository : JpaRepository<IapsPerson, Long>