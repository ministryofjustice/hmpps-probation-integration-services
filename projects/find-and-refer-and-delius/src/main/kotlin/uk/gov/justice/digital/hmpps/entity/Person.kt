package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate

@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsNumber: String?,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

)
