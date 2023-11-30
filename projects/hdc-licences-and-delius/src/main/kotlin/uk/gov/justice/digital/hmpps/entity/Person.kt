package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

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
    val prisonManagers: List<PrisonManager> = listOf()
)
