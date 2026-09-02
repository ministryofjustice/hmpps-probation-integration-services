package uk.gov.justice.digital.hmpps.entity.person

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

@Entity
@Table(name = "personal_circumstance")
@Immutable
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "circumstance_type_id")
    val type: PersonalCircumstanceType,

    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id")
    val subType: PersonalCircumstanceSubType? = null,

    @Column(name = "start_date")
    val startDate: ZonedDateTime,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,

    @jakarta.persistence.Lob
    @Column(name = "notes")
    val notes: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Convert(converter = YesNoConverter::class)
    val evidenced: Boolean? = false
)

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
)

@Entity
@Table(name = "r_circumstance_sub_type")
@Immutable
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,
    @Column(name = "code_description")
    val description: String
)

interface PersonalCircumstanceRepository : JpaRepository<PersonalCircumstance, Long> {
    @Query(
        """
        select pc
        from PersonalCircumstance pc
        where pc.person.crn = :crn
          and pc.softDeleted = false
          and (:activeOnly = false or pc.endDate is null or pc.endDate > current_date)
        order by pc.startDate
        """
    )
    fun findByCrnOrderByStartDate(
        @Param("crn") crn: String,
        @Param("activeOnly") activeOnly: Boolean
    ): List<PersonalCircumstance>
}