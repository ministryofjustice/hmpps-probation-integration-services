package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class PersonAlias(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "first_name")
    val firstName: String,

    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @Column(name = "second_name")
    val secondName: String?,

    @Column(name = "third_name")
    val thirdName: String?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "alias_id")
    val id: Long,
) {
    companion object {
        val FORENAME = PersonAlias::firstName.name
        val SURNAME = PersonAlias::surname.name
        val DOB = PersonAlias::dateOfBirth.name
    }
}