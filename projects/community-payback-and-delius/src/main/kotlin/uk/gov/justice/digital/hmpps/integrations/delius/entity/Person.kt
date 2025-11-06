package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@Table(name = "offender")
@Immutable
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long? = null,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "current_exclusion", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentExclusion: Boolean = false,

    @Column(name = "current_restriction", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean = false,

    @Column(name = "exclusion_message")
    val exclusionMessage: String? = null,

    @Column(name = "restriction_message")
    val restrictionMessage: String? = null,

    )