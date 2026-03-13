package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@SQLRestriction("soft_deleted = 0")
class Release(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @Column(name = "actual_release_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
