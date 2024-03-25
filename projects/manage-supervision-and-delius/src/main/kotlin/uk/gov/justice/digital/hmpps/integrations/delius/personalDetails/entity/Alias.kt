package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class Alias(
    @Id
    @Column(name = "alias_id")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    val softDeleted: Boolean = false
)

interface AliasRepository : JpaRepository<Alias, Long> {
    fun findByPersonId(personId: Long): List<Alias>
}
