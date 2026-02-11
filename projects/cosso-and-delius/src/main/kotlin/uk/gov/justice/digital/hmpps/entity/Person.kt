package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val offenderId: Long = 0,
    @Column(name = "crn", nullable = false, columnDefinition = "CHAR(7)")
    val crn: String,
    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,
    val firstName: String,
    val secondName: String? = null,
    val thirdName: String? = null,
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    val telephoneNumber: String? = null,
    val mobileNumber: String? = null,
    @Column(name = "e_mail_address")
    val emailAddress: String? = null,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
) {
    fun middleName() = listOfNotNull(secondName, thirdName).joinToString(" ")
}

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}
