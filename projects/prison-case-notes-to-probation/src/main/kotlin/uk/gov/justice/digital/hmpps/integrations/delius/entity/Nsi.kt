package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Immutable
@Entity
class Nsi(
    @Id
    @Column(name = "nsi_id")
    var id: Long,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", updatable = false)
    val type: NsiType,

    val referralDate: LocalDate,

    val offenderId: Long,
    val eventId: Long? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_nsi_type")
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long,

    val code: String
)

@Immutable
@Entity
@Table(name = "r_nomis_type_nsi_type")
class NomisTypeNsiType(
    @Id
    @Column(name = "nomis_type_nsi_type_id")
    val id: Long,

    @Column(name = "nomis_contact_type")
    val caseNoteType: String,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", updatable = false)
    val nsiType: NsiType,

    @Enumerated(EnumType.STRING)
    val nsiLevel: NsiLevel = NsiLevel.OFFENDER

) {
    enum class NsiLevel {
        OFFENDER, EVENT
    }
}
