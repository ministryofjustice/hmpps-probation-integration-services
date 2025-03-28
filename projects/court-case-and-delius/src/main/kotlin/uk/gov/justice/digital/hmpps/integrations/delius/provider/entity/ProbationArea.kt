package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Institution

@Entity
@Immutable
@Table(name = "probation_area")
class ProbationAreaEntity(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Column(name = "private", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val privateSector: Boolean,

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    val organisation: Organisation,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @OneToMany(mappedBy = "probationArea")
    val boroughs: List<Borough> = listOf(),

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    val institution: Institution? = null,

    @OneToMany
    @JoinColumn(name = "probation_area_id")
    val teams: List<Team> = emptyList(),

    @OneToMany
    @JoinColumn(name = "probation_area_id")
    val providerTeams: List<ProviderTeam> = emptyList()
)

@Immutable
@Entity
@Table
class ProviderTeam(

    @Id
    @Column(name = "provider_team_id")
    val providerTeamId: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "name")
    val name: String,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @ManyToOne
    @JoinColumn(name = "external_provider_id")
    val externalProvider: ExternalProvider
)

@Immutable
@Entity
@Table
class ExternalProvider(

    @Id @Column(name = "external_provider_id")
    val externalProviderId: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table
class LocalDeliveryUnit(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "local_delivery_unit_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @OneToMany(mappedBy = "borough")
    val districts: List<District> = listOf(),

    )

@Immutable
@Entity
@Table
class Organisation(

    @Id
    @Column(name = "organisation_id")
    val organisationId: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean,

    )


