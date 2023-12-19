package uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@Entity
@SequenceGenerator(name = "domain_event_id_seq", sequenceName = "domain_event_id_seq", allocationSize = 1)
class DomainEvent(

    @Column(columnDefinition = "varchar2(4000)")
    val messageBody: String,

    @Column(columnDefinition = "varchar2(4000)")
    val messageAttributes: String,

    @Id
    @Column(name = "domain_event_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain_event_id_seq")
    val id: Long = 0
) {
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()
}

interface DomainEventRepository : JpaRepository<DomainEvent, Long>
