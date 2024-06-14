package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "CHAR(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @OneToOne(mappedBy = "person")
    val manager: PersonManager? = null,

    @OneToMany(mappedBy = "person")
    val additionalIdentifier: List<AdditionalIdentifier> = emptyList()

)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "additional_identifier")
class AdditionalIdentifier(

    @Id
    @Column(name = "additional_identifier_id")
    val id: Long,

    @Column(name = "identifier")
    val crn: String,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "identifier_name_id")
    val type: ReferenceData
)
