package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.OffenderPersonalityDisorder
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "nsi")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Nsi(
    @Id
    @Column(name = "nsi_id", nullable = false)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", nullable = false)
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val status: NsiStatus,

    @Lob
    @Column
    val notes: String? = null,

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    fun toOffenderPersonalityDisorder(): OffenderPersonalityDisorder {
        return OffenderPersonalityDisorder(
            status = CodeAndDescription(
                code = this.status.code,
                description = this.status.description
            )
        )
    }
}

@Entity
@Immutable
@Table(name = "r_nsi_type")
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long = 0,

    @Column(name = "code")
    val code: String
)

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    @Id
    @Column(name = "nsi_status_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String
)

interface NsiRepository : JpaRepository<Nsi, Long> {

    @Query(
        """
        select nsi from Nsi nsi
        join fetch nsi.status
        join fetch nsi.type
        where nsi.person.crn = :crn
        and nsi.type.code = 'OPD1'
    """
    )
    fun findOffenderPersonalityDisorderByCrn(crn: String): Nsi?
}

