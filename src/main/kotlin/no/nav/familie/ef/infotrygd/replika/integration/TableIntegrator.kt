package no.nav.familie.ef.infotrygd.replika.integration

import org.hibernate.boot.Metadata
import org.hibernate.boot.spi.BootstrapContext
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.integrator.spi.Integrator
import org.hibernate.jpa.boot.spi.IntegratorProvider
import org.hibernate.mapping.Column
import org.hibernate.service.spi.SessionFactoryServiceRegistry
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component

@Component
class TableIntegrator : Integrator {
    private lateinit var _tables: Map<String, List<String>>

    val tables: Map<String, List<String>>
        get() {
            return _tables
        }

    override fun integrate(
        metadata: Metadata,
        bootstrapContext: BootstrapContext,
        sessionFactory: SessionFactoryImplementor,
    ) {
        val result = mutableMapOf<String, List<String>>()

        for (
        namespace in metadata
            .getDatabase()
            .getNamespaces()
        ) {
            for (table in namespace.getTables()) {
                val cols = table.columns.toList()
                val names = cols.map { (it as Column).canonicalName }
                result[table.name] = names
            }
        }
        _tables = result
    }

    override fun disintegrate(
        p0: SessionFactoryImplementor,
        p1: SessionFactoryServiceRegistry,
    ) {
    }
}

@Component
class TableIntegratorProvider(
    private val tableIntegrator: TableIntegrator,
) : IntegratorProvider {
    override fun getIntegrators(): MutableList<Integrator> = mutableListOf(tableIntegrator)
}

@Component
class HibernateConfig(
    private val tableIntegratorProvider: TableIntegratorProvider,
) : HibernatePropertiesCustomizer {
    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties["hibernate.integrator_provider"] = tableIntegratorProvider
    }
}
