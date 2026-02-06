package uk.gov.justice.digital.hmpps.jpa

import org.hibernate.boot.Metadata
import org.hibernate.boot.spi.BootstrapContext
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.integrator.spi.Integrator
import org.hibernate.jpa.boot.spi.IntegratorProvider
import org.hibernate.mapping.SimpleValue
import org.hibernate.mapping.ToOne
import kotlin.reflect.full.memberProperties

/**
 * A Hibernate Integrator to apply Kotlin non-nullability to the Hibernate mappings. This is equivalent to setting
 * `nullable = false` on the `@Column` annotations for non-nullable Kotlin fields.
 *
 * This is to avoid "Schema validation: column defined as not-null in the database, but nullable in model" errors
 */
class KotlinNullabilityIntegrator : Integrator {
    override fun integrate(
        metadata: Metadata,
        bootstrapContext: BootstrapContext,
        sessionFactory: SessionFactoryImplementor
    ) {
        for (entity in metadata.entityBindings) {
            val kotlinNullability =
                entity.mappedClass.kotlin.memberProperties.associate { it.name to it.returnType.isMarkedNullable }

            entity.properties
                .filter { it.value is SimpleValue || it.value is ToOne }
                .filter { !it.isSynthetic && !it.isBackRef }
                .forEach { property ->
                    if (kotlinNullability[property.name] == false) {
                        property.resetOptional(false)
                    }
                }
        }
    }
}

class KotlinNullabilityIntegratorProvider : IntegratorProvider {
    override fun getIntegrators() = listOf(KotlinNullabilityIntegrator())
}
