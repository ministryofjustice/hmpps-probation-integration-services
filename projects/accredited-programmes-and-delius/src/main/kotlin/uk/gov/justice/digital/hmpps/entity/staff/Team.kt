package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue
import java.time.LocalDate

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val localAdminUnit: LocalAdminUnit,

    @ManyToMany
    @JoinTable(
        name = "team_office_location",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "office_location_id")]
    )
    val officeLocations: List<OfficeLocation>,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    val endDate: LocalDate?,
) {
    fun toCodedValue() = CodedValue(code, description)
}