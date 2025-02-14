package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,

    @OneToMany(mappedBy = "person")
    val addresses: List<PersonAddress>,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_id")
    val id: Long,
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)