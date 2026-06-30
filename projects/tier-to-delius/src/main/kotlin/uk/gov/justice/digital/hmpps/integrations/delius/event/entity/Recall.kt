package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@SQLRestriction("soft_deleted = 0")
class Recall(
    @Id
    @Column(name = "recall_id")
    val id: Long,

    @Column(name = "recall_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "release_id")
    val release: Release? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
