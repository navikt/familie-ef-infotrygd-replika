package no.nav.familie.ef.infotrygd.replika.exodus

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Konfigurasjon for klienten mot historisk-exodus. Se application.yml for hvilke miljøvariabler
 * som fyller disse (EXODUS_BASE_URL, EXODUS_SCOPE m.fl.).
 */
@ConfigurationProperties(prefix = "exodus")
data class ExodusProperties(
    /** Base-URL til historisk-exodus, f.eks. https://historisk-exodus.<cluster>.<namespace>... */
    val baseUrl: String,
    /**
     * Antall rader vi ber om per kall til /api/hentUttrekk.
     *
     * NB: navngitt uten ø/æ/å siden Spring sin relaxed binding av @ConfigurationProperties matcher
     * kebab-case i YAML mot camelCase her rent ASCII-basert.
     */
    val batchStorrelse: Int = 1000,
    /** Cron-uttrykk for hvor ofte scheduleren forsøker å replikere alle tabellene. */
    val schedulerCron: String = "0 */5 * * * *",
    /** Maks antall sider (kall mot exodus) som hentes for én tabell per scheduler-kjøring. */
    val maksSiderPerKjoring: Int = 500,
    /**
     * Om scheduleren faktisk skal kjøre replikering. Default false slik at en nydeployet
     * instans ikke begynner å lese data automatisk - må skrus på eksplisitt (f.eks. via
     * miljøvariabelen EXODUS_SCHEDULER_ENABLED).
     */
    val schedulerEnabled: Boolean = false,
)
