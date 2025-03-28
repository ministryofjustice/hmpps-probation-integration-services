package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class Alias(
    @Column(name = "offender_id")
    val personId: Long,
    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
    @Id
    @Column(name = "alias_id")
    val id: Long
)

interface AliasRepository : JpaRepository<Alias, Long> {
    fun findByPersonId(personId: Long): List<Alias>
}