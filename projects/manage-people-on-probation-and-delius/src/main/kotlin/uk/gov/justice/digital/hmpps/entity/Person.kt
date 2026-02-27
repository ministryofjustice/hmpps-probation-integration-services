package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.model.shared.Name

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

    val firstName: String,
    val secondName: String? = null,
    val thirdName: String? = null,
    val surname: String,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun name() = Name(firstName, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
}
