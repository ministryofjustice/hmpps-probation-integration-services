package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsNumber: String,

    @OneToMany(mappedBy = "person")
    @SQLRestriction("active_flag = 1 and soft_deleted = 0")
    val communityManagers: List<CommunityManagerEntity> = listOf(),

    @OneToMany(mappedBy = "person")
    @SQLRestriction("active_flag = 1 and soft_deleted = 0")
    val prisonManagers: List<PrisonManager> = listOf(),

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
