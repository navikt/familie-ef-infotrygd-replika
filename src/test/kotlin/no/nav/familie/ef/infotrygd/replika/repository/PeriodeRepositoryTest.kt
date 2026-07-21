package no.nav.familie.ef.infotrygd.replika.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ef.infotrygd.replika.model.StønadType
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdEndringKode
import no.nav.familie.ef.infotrygd.replika.rest.api.InfotrygdSakstype
import no.nav.familie.ef.infotrygd.replika.rest.api.PeriodeRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrasjonstest")
internal class PeriodeRepositoryTest {
    @Autowired
    lateinit var periodeRepository: PeriodeRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private val startdato = LocalDate.now().minusYears(1)
    private val sluttdato = LocalDate.now().plusYears(1)

    private val fnr = "01234567890"

    @AfterEach
    fun tearDown() {
        listOf(
            "T_ROLLE",
            "T_LOPENR_FNR",
            "T_VEDTAK",
            "T_STONAD",
            "T_ENDRING",
            "T_EF",
            "T_BEREGN_GRL",
        ).forEach {
            jdbcTemplate.update("TRUNCATE TABLE $it")
        }
    }

    @Test
    fun `hent tom liste barnetilsynbarn gitt ingen barnetilsynperioder`() {
        val barn = periodeRepository.hentBarnForPerioder(emptyList())
        assertThat(barn).isEmpty()
    }

    @Test
    fun `hent barnetilsyn-barn gitt barnetilsynperioder som finnes`() {
        insertBarnetilsynsak()

        val perioder =
            periodeRepository
                .hentPerioder(
                    PeriodeRequest(
                        setOf(FoedselsNr(fnr)),
                        setOf(StønadType.BARNETILSYN),
                    ),
                ).groupBy({ it.first }) { it.second }
                .toMutableMap()
        assertThat(perioder).isNotEmpty
        val barn = periodeRepository.hentBarnForPerioder(perioder.getOrDefault(StønadType.BARNETILSYN, emptyList()))
        assertThat(barn[2]).containsExactly("01234567891", "01234567892")
    }

    @Test
    fun `hentPersonerForMigrering går fint`() {
        periodeRepository.hentPersonerForMigrering(10)
    }

    @Test
    fun `skal hente perioder`() {
        opprettPeriodeOvergangsstønad()

        val perioder =
            periodeRepository.hentPerioder(
                PeriodeRequest(
                    setOf(FoedselsNr(fnr)),
                    setOf(StønadType.OVERGANGSSTØNAD),
                ),
            )
        assertThat(perioder).hasSize(1)
        assertThat(perioder.first().first).isEqualTo(StønadType.OVERGANGSSTØNAD)
        assertThat(perioder.first().second.oppdragId).isEqualTo(1)
    }

    @Test
    fun `henting av perioder er riktig`() {
        opprettPeriodeOvergangsstønad()

        val perioder = hentPerioder()
        assertThat(perioder).hasSize(1)
        val periode = perioder.first().second
        assertThat(periode.inntektsgrunnlag).isEqualTo(100)
        assertThat(periode.sakstype).isEqualTo(InfotrygdSakstype.SØKNAD)
    }

    @Test
    fun `henting av perioder uten T_BEREGN_GRL gir 0 i inntektsgrunnlag`() {
        lagVedtak(stønadType = "EO", vedtakId = 1, stønadId = 1)

        val perioder = hentPerioder()
        assertThat(perioder).hasSize(1)
        assertThat(perioder.first().second.inntektsgrunnlag).isEqualTo(0)
    }

    @Test
    fun `hentPerioder returnerer en rad per t_endring-kode når vedtaket har flere endringskoder`() {
        lagVedtak(stønadType = "EO", vedtakId = 1, stønadId = 1)
        // lagVedtak har allerede satt inn en 'F ' (FØRSTEGANGSVEDTAK)-rad.
        // Legger til en 'O ' (OPPHØRT)-rad for samme vedtak_id, slik det observeres i produksjon
        // når et vedtak i ettertid får registrert en avsluttende status. hentPerioder() er rådata
        // og skal fortsatt returnere begge radene - det er sammenslåingslogikken (InfotrygdPeriodeUtil)
        // som avgjør hvilken kode som skal gjelde.
        jdbcTemplate.update("INSERT INTO t_endring (vedtak_id, kode) VALUES (?, 'O ')", 1)

        val perioder = hentPerioder()

        assertThat(perioder).hasSize(2)
        assertThat(perioder.map { it.second.kode })
            .containsExactlyInAnyOrder(InfotrygdEndringKode.FØRSTEGANGSVEDTAK, InfotrygdEndringKode.OPPHØRT)
    }

    @Test
    fun `periode med oppdrag_id = null`() {
        lagVedtak(stønadType = "EB", vedtakId = 2, stønadId = 2, oppdragId = null)

        val hentPerioder =
            periodeRepository.hentPerioder(
                PeriodeRequest(
                    setOf(FoedselsNr(fnr)),
                    setOf(StønadType.BARNETILSYN),
                ),
            )
        val perioder = hentPerioder.map { it.second }

        assertThat(perioder).hasSize(1)
        assertThat(perioder.single().oppdragId).isNull()
    }

    private fun hentPerioder() =
        periodeRepository.hentPerioder(
            PeriodeRequest(
                setOf(FoedselsNr(fnr)),
                StønadType.entries.toSet(),
            ),
        )

    private fun opprettPeriodeOvergangsstønad() {
        lagVedtak(stønadType = "EO", vedtakId = 1, stønadId = 1)
        jdbcTemplate.update(
            "INSERT INTO t_beregn_grl (vedtak_id, type_belop, fom, belop, brukerid) " +
                "VALUES (1,'ARBM',CURRENT_DATE, 100, 'A')",
        )
        jdbcTemplate.update(
            "INSERT INTO t_beregn_grl (vedtak_id, type_belop, fom, belop, brukerid) " +
                "VALUES (1,'ABCD',CURRENT_DATE, 50, 'A')",
        )
    }

    private fun lagVedtak(
        stønadType: String,
        vedtakId: Int,
        stønadId: Int,
        oppdragId: Int? = 1,
    ) {
        jdbcTemplate.update("INSERT INTO t_lopenr_fnr (person_lopenr, personnr) VALUES (1,  ?)", fnr)
        jdbcTemplate.update(
            """INSERT INTO t_vedtak (vedtak_id, person_lopenr, stonad_id, kode_rutine, kode_resultat, 
                dato_innv_fom, dato_innv_tom, brukerid, type_sak, tidspunkt_reg)
                          VALUES (?,1,?,?,'I',?,?, 'NISSEN', 'S ', CURRENT_TIMESTAMP)""",
            vedtakId,
            stønadId,
            stønadType,
            startdato,
            sluttdato,
        )
        jdbcTemplate.update(
            """INSERT INTO t_stonad (stonad_id, oppdrag_id, person_lopenr, dato_start, dato_opphor)
                                         VALUES (?, ?, 1, CURRENT_DATE, NULL)""",
            stønadId,
            oppdragId,
        )
        jdbcTemplate.update("INSERT INTO t_endring (vedtak_id, kode) VALUES (?, 'F ')", vedtakId)
        jdbcTemplate.update(
            "INSERT INTO t_ef (vedtak_id, stonad_belop, innt_fradrag, netto_belop, sam_fradrag, kode_overg, " +
                "aktivitet, barnt_utg) VALUES (?,1,1,1,1,' ',' ', 1)",
            vedtakId,
        )
    }

    private fun insertBarnetilsynsak() {
        // Lag barnetilsynvedtak
        lagVedtak(stønadType = "EB", vedtakId = 2, stønadId = 2)
        // legg til barn
        jdbcTemplate.update("INSERT INTO t_lopenr_fnr (person_lopenr, personnr) VALUES (2, '01234567891')")
        jdbcTemplate.update("INSERT INTO t_lopenr_fnr (person_lopenr, personnr) VALUES (3, '01234567892')")

        // barnetilsynbarn 1 på barnetilsynvedtak
        jdbcTemplate.update(
            "INSERT INTO t_rolle (vedtak_id,type,tidspunkt_reg,fom,tom,person_lopenr_r,brukerid,barn_type," +
                "bor_sammen_med,trygdetid_faktisk,trygdetid_anvendt,trygdetid_unntak,trygd_medlem_siden," +
                "utenlandsopphold,bt_1_sum,bt_1_antall,bt_2_sum,bt_2_antall,bt_s_sum,bt_s_antall,opprettet,oppdatert) " +
                "VALUES (2,'EB',CURRENT_TIMESTAMP, CURRENT_DATE,CURRENT_DATE,2,'MIA4408',  NULL, NULL, NULL,NULL,NULL," +
                "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL, CURRENT_DATE,CURRENT_DATE)",
        )

        // barnetilsynbarn 2 på barnetilsynvedtak
        jdbcTemplate.update(
            "INSERT INTO t_rolle (vedtak_id,type,tidspunkt_reg,fom,tom,person_lopenr_r,brukerid,barn_type," +
                "bor_sammen_med,trygdetid_faktisk,trygdetid_anvendt,trygdetid_unntak,trygd_medlem_siden," +
                "utenlandsopphold,bt_1_sum,bt_1_antall,bt_2_sum,bt_2_antall,bt_s_sum,bt_s_antall,opprettet,oppdatert) " +
                "VALUES (2,'EB',CURRENT_TIMESTAMP, CURRENT_DATE,CURRENT_DATE,3,'MIA4409',  NULL, NULL, NULL,NULL,NULL," +
                "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL, CURRENT_DATE,CURRENT_DATE)",
        )
    }
}
