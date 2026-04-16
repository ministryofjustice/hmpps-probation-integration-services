package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val type: ReferenceData,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
