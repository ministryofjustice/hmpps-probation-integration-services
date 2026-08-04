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
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "disability")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Disability(
    @Id
    @Column(name = "disability_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_type_id")
    val type: ReferenceData,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun toModelDisability(): uk.gov.justice.digital.hmpps.controller.model.Disability {
        return uk.gov.justice.digital.hmpps.controller.model.Disability(
            type = this.type.toCodeAndDescription(),
            updatedAt = this.lastUpdated
        )
    }
}

interface DisabilityRepository : JpaRepository<Disability, Long> {
    fun findByPersonId(personId: Long): List<Disability>
}