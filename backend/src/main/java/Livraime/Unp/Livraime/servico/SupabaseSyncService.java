package Livraime.Unp.Livraime.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SupabaseSyncService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseSyncService.class);

    private final JdbcTemplate local;
    private final JdbcTemplate external;
    private final AsyncDatabaseService asyncDatabaseService;

    public SupabaseSyncService(JdbcTemplate localJdbcTemplate,
                               @Qualifier("externalJdbcTemplate") JdbcTemplate externalJdbcTemplate,
                               AsyncDatabaseService asyncDatabaseService) {
        this.local = localJdbcTemplate;
        this.external = externalJdbcTemplate;
        this.asyncDatabaseService = asyncDatabaseService;
    }

    // Roda a cada 1 hora (em ms: 60 * 60 * 1000)
    @Scheduled(fixedDelay = 3600000, initialDelay = 10000)
    public void syncAll() {
        if (!asyncDatabaseService.isExternalAvailable()) {
            log.warn("[Sync] Supabase indisponível, sync adiado.");
            return;
        }

        log.info("[Sync] Iniciando sincronização com Supabase...");

        syncTable("admin",        "id, email, nivel_acesso, nome, senha");
        syncTable("usuario",      "id, ativo, codigo_verificacao, cpf, data_cadastro, email, email_verificado, city, complement, neighborhood, endereco_numero, state, street, zip_code, nome, plano, senha, area_code, telefone_numero");
        syncTable("usuario_roles","usuario_id, roles");
        syncTable("beneficiados", "id, ativo, cliente_assinante_id, data_nascimento, descricao_necessidades, estado, idade, nome");
        syncTable("subscriptions","id, created_at, usuario_id");
        syncTable("donations",    "id, donated_at, quantidade, usuario_id");
        syncTable("parceiros",    "id, active, created_at, deleted_at, descricao_servicos, email, endereco, nome, telefone, tipo");

        log.info("[Sync] Sincronização concluída.");
    }

    private void syncTable(String table, String columns) {
        try {
            List<Map<String, Object>> rows = local.queryForList("SELECT " + columns + " FROM " + table);

            if (rows.isEmpty()) {
                log.info("[Sync] Tabela '{}' vazia, nada a sincronizar.", table);
                return;
            }

            // Limpa a tabela no Supabase e reinsere tudo (full replace)
            external.execute("DELETE FROM " + table);

            String[] cols = columns.split(",\\s*");
            String placeholders = "?,".repeat(cols.length);
            placeholders = placeholders.substring(0, placeholders.length() - 1);
            String insertSql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

            for (Map<String, Object> row : rows) {
                Object[] values = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    values[i] = row.get(cols[i].toUpperCase());
                }
                external.update(insertSql, values);
            }

            log.info("[Sync] Tabela '{}' sincronizada — {} registros.", table, rows.size());

        } catch (Exception e) {
            log.error("[Sync] Falha ao sincronizar tabela '{}': {}", table, e.getMessage());
        }
    }
}