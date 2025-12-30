package uk.gov.justice.digital.hmpps.data.loader

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.data.manager.DataManagerInterface

@Component
@ConditionalOnProperty("dev.dataloader.enabled")
abstract class BaseDataLoader(
    private val dataManager: DataManager
) : DataManagerInterface by dataManager, ApplicationListener<ApplicationReadyEvent> {
    abstract fun setupData()
    abstract fun systemUser(): Any?

    @PostConstruct
    fun saveAuditUser() {
        systemUser()?.let { dataManager.save(it) }
    }

    @Transactional
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        setupData()
    }
}