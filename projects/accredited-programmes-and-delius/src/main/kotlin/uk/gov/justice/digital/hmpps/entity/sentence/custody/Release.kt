package uk.gov.justice.digital.hmpps.entity.sentence.custody

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Release(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "release_type_id")
    val type: ReferenceData,

    @Column(name = "actual_release_date")
    val date: LocalDate,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)