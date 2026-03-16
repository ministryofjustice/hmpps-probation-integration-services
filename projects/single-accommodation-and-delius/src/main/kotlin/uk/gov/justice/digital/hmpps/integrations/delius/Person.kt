package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val noms: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @ManyToOne
    @JoinColumn(name = "current_tier")
    val currentTier: ReferenceData?,
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByIdIn(ids: List<Long>): List<Person>
}
