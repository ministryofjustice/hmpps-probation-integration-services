package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.PersonCircumstance
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "personal_circumstance")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_type_id")
    val type: PersonalCircumstanceType,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_sub_type_id")
    val subType: PersonalCircumstanceSubType,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    val startDate: LocalDate,

    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    ) {
    fun toPersonCircumstance(): PersonCircumstance {
        return PersonCircumstance(
            type = this.type.toCodeAndDescription(),
            subType = this.subType.toCodeAndDescription(),
            updatedAt = this.lastUpdated
        )
    }
}

interface PersonCircumstanceRepository : JpaRepository<PersonalCircumstance, Long> {

    @Query(
        """
        select pc from PersonalCircumstance pc 
        left join fetch pc.subType sub
        left join fetch pc.type type
        where pc.personId = :personId
    """
    )
    fun findCurrentCircumstances(personId: Long): List<PersonalCircumstance>

}

@Immutable
@Entity
@Table(name = "r_circumstance_sub_type")
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,

    @Column(name = "code_description")
    val description: String,
) {
    fun toCodeAndDescription(): CodeAndDescription {
        return CodeAndDescription(
            code = this.id.toString(),
            description = this.description
        )
    }
}

@Entity
@Table(name = "r_circumstance_type")
@Immutable
class PersonalCircumstanceType(
    @Id
    @Column(name = "circumstance_type_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String
) {
    fun toCodeAndDescription(): CodeAndDescription {
        return CodeAndDescription(
            code = this.code,
            description = this.description
        )
    }
}