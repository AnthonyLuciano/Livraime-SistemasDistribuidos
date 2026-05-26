package Livraime.Unp.Livraime.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class DatabaseConfig {

    // --- Datasource Primário ---

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @Primary
    public JdbcTemplate localJdbcTemplate(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        return new JdbcTemplate(primaryDataSource);
    }

    // --- Datasource Externo ---

    @Bean
    @ConfigurationProperties("spring.external-datasource")
    public DataSourceProperties externalDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource externalDataSource() {
        HikariDataSource ds = (HikariDataSource) externalDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setInitializationFailTimeout(0); // não trava a inicialização se offline
        ds.setConnectionTimeout(3000);      // desiste após 3 segundos
        return ds;
    }

    @Bean
    public JdbcTemplate externalJdbcTemplate(@Qualifier("externalDataSource") DataSource externalDataSource) {
        return new JdbcTemplate(externalDataSource);
    }

    // --- Thread Pool ---

    @Bean("dbTaskExecutor")
    public TaskExecutor dbTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("db-async-");
        executor.initialize();
        return executor;
    }
}