package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Event
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.OrderManager

interface EventRepository : JpaRepository<Event, Long>
interface DisposalRepository : JpaRepository<Disposal, Long>
interface DisposalTypeRepository : JpaRepository<DisposalType, Long>
interface OrderManagerRepository : JpaRepository<OrderManager, Long>
