package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.model.Name
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
    val exclusionMessage: String?,
    val restrictionMessage: String?,
)

fun Person.name() = Name(firstName, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
