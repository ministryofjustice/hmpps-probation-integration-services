package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class PersonEvent(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @OneToMany(mappedBy = "offenderId")
    val events: List<Event>,

    @OneToMany(mappedBy = "personId")
    val nsis: List<Nsi>,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface PersonEventRepository : JpaRepository<PersonEvent, Long> {

    @Query(
        """
            select pe from PersonEvent pe
        """
    )
    fun findAllCrns(pageable: Pageable): Page<PersonEvent>
    fun findByCrnIn(crns: List<String>, pageable: Pageable): Page<PersonEvent>
}
