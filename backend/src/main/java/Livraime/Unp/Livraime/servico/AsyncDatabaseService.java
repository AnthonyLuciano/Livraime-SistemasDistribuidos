package Livraime.Unp.Livraime.servico;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncDatabaseService {

    private static final Logger log = LoggerFactory.getLogger(AsyncDatabaseService.class);

    private final JdbcTemplate localJdbcTemplate;
    private final JdbcTemplate externalJdbcTemplate;

    public AsyncDatabaseService(JdbcTemplate localJdbcTemplate,
                                @Qualifier("externalJdbcTemplate") JdbcTemplate externalJdbcTemplate) {
        this.localJdbcTemplate = localJdbcTemplate;
        this.externalJdbcTemplate = externalJdbcTemplate;
    }

    /** Testa se o banco externo está acessível sem lançar exceção. */
    public boolean isExternalAvailable() {
        try {
            externalJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.warn("[AsyncDatabaseService] Banco externo inacessível: {}", e.getMessage());
            return false;
        }
    }

    @Async("dbTaskExecutor")
    public CompletableFuture<Integer> executeLocalUpdate(String sql, Object... args) {
        return CompletableFuture.completedFuture(localJdbcTemplate.update(sql, args));
    }

    @Async("dbTaskExecutor")
    public CompletableFuture<Long> queryLocalCount(String sql) {
        return CompletableFuture.completedFuture(localJdbcTemplate.queryForObject(sql, Long.class));
    }

    @Async("dbTaskExecutor")
    public CompletableFuture<Integer> executeExternalUpdate(String sql, Object... args) {
        return CompletableFuture.completedFuture(externalJdbcTemplate.update(sql, args));
    }

    @Async("dbTaskExecutor")
    public CompletableFuture<List<Map<String, Object>>> queryExternal(String sql) {
        return CompletableFuture.completedFuture(externalJdbcTemplate.queryForList(sql));
    }

    @Async("dbTaskExecutor")
    public CompletableFuture<Void> initExternalSchema() {
        externalJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS external_metrics (
                    id BIGSERIAL PRIMARY KEY,
                    metric_name VARCHAR(255),
                    metric_value BIGINT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        return CompletableFuture.completedFuture(null);
    }
}