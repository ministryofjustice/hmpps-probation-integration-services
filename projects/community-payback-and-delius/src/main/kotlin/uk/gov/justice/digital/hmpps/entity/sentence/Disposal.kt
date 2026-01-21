package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Entity
@Table(name = "disposal")
@Immutable
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "disposal_date")
    val date: LocalDate,

    val length: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "disposal_type_code")
    val code: String,

    val description: String,

    val ftcLimit: Long?,

    @Column(name = "pre_cja2003")
    @Convert(converter = YesNoConverter::class)
    val preCja2003: Boolean = false
)