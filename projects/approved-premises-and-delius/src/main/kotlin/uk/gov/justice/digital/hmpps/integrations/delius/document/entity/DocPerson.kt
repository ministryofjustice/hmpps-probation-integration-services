package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "offender")
class DocPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "CHAR(7)")
    val crn: String,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)

interface DocPersonRepository : JpaRepository<DocPerson, Long> {
    fun findByCrn(crn: String): DocPerson?
}
