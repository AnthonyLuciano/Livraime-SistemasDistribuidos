package Livraime.Unp.Livraime.config;

import Livraime.Unp.Livraime.modelo.Parceiro;
import Livraime.Unp.Livraime.repositorio.PartnerRepository;
import Livraime.Unp.Livraime.servico.AsyncDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

@Component
public class MetricsDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MetricsDataSeeder.class);

    @Autowired
    private AsyncDatabaseService asyncDatabaseService;

    @Autowired
    private PartnerRepository partnerRepository;

    private final Random random = new Random();

    @Override
    public void run(ApplicationArguments args) {
        try {
            runSeed();
        } catch (Exception e) {
            log.warn("[MetricsDataSeeder] Seed ignorado: {}", e.getMessage());
        }
    }

    private void runSeed() throws Exception {
        Long subs  = getCountSafely("SELECT COUNT(*) FROM subscriptions");
        Long dons  = getCountSafely("SELECT COUNT(*) FROM donations");
        Long parts = getCountSafely("SELECT COUNT(*) FROM parceiros");

        if ((subs != null && subs > 0) || (dons != null && dons > 0) || (parts != null && parts > 0)) {
            log.info("[MetricsDataSeeder] Tabelas já populadas, seed ignorado.");
            return;
        }

        // Verifica banco externo UMA VEZ antes de qualquer operação nele
        boolean externalOk = asyncDatabaseService.isExternalAvailable();
        if (!externalOk) {
            log.warn("[MetricsDataSeeder] Banco externo offline — seed externo será ignorado.");
        } else {
            try {
                asyncDatabaseService.initExternalSchema().get();
            } catch (Exception e) {
                log.warn("[MetricsDataSeeder] Falha ao criar schema externo: {}", e.getMessage());
                externalOk = false;
            }
        }

        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            LocalDate dataDoMes   = hoje.minusMonths(i);
            LocalDate inicioDoMes = dataDoMes.withDayOfMonth(1);
            LocalDate fimDoMes    = dataDoMes.withDayOfMonth(dataDoMes.lengthOfMonth());

            int numSubs = 5 + random.nextInt(16);
            for (int s = 0; s < numSubs; s++) {
                LocalDateTime dt = randomDateTimeBetween(inicioDoMes, fimDoMes);
                asyncDatabaseService.executeLocalUpdate(
                        "INSERT INTO subscriptions (created_at, usuario_id) VALUES (?, ?)",
                        Timestamp.valueOf(dt), null).get();
            }

            int numDon = 3 + random.nextInt(8);
            for (int d = 0; d < numDon; d++) {
                LocalDateTime dt = randomDateTimeBetween(inicioDoMes, fimDoMes);
                int quantidade = 1 + random.nextInt(10);
                asyncDatabaseService.executeLocalUpdate(
                        "INSERT INTO donations (donated_at, quantidade, usuario_id) VALUES (?, ?, ?)",
                        Timestamp.valueOf(dt), quantidade, null).get();
            }

            if (externalOk) {
                try {
                    asyncDatabaseService.executeExternalUpdate(
                            "INSERT INTO external_metrics (metric_name, metric_value) VALUES (?, ?)",
                            "subscriptions_month_" + dataDoMes.getMonthValue() + "_" + dataDoMes.getYear(),
                            numSubs).get();
                    asyncDatabaseService.executeExternalUpdate(
                            "INSERT INTO external_metrics (metric_name, metric_value) VALUES (?, ?)",
                            "donations_month_" + dataDoMes.getMonthValue() + "_" + dataDoMes.getYear(),
                            numDon).get();
                } catch (Exception e) {
                    log.warn("[MetricsDataSeeder] Falha ao gravar métrica externa: {}", e.getMessage());
                }
            }

            int numParts = random.nextInt(5);
            for (int p = 0; p < numParts; p++) {
                String nome = "Parceiro Mock " + (i * 10 + p + 1);
                String tipo = random.nextBoolean() ? "sebo" : "autor";
                Parceiro parceiro = new Parceiro(null, nome, tipo, "Rua Exemplo, 123",
                        "+55 11 99999-0000", "mock@parceiro.local", "Serviços de exemplo", true);
                parceiro.setCreatedAt(randomDateTimeBetween(inicioDoMes, fimDoMes));
                partnerRepository.save(parceiro);
            }
        }

        log.info("[MetricsDataSeeder] Seed concluído.");
    }

    private Long getCountSafely(String sql) {
        try {
            return asyncDatabaseService.queryLocalCount(sql).get();
        } catch (Exception e) {
            log.warn("[MetricsDataSeeder] Não foi possível consultar '{}': {}", sql, e.getMessage());
            return null;
        }
    }

    private LocalDateTime randomDateTimeBetween(LocalDate start, LocalDate end) {
        int days = (int) (end.toEpochDay() - start.toEpochDay());
        int add  = days > 0 ? random.nextInt(days + 1) : 0;
        LocalDate chosen = start.plusDays(add);
        return LocalDateTime.of(chosen, LocalTime.of(
                random.nextInt(24), random.nextInt(60), random.nextInt(60)));
    }
}