package no.nav.familie.ef.infotrygd.replika.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Suppress("unused") // brukes av hibernate for å generere hvilke tabeller som brukes
@Entity
@Table(name = "T_STONAD")
data class Stønad(
    @Id
    @Column(name = "STONAD_ID")
    val id: Long,
    @Column(name = "PERSON_LOPENR")
    val personKey: Long,
    @Column(name = "OPPDRAG_ID")
    val oppdragId: Long,
    @Column(name = "KODE_RUTINE")
    val kodeRutine: String,
    @Column(name = "DATO_START")
    val datoStart: LocalDate,
    @Column(name = "DATO_OPPHOR")
    val datoOpphør: LocalDate,
)
