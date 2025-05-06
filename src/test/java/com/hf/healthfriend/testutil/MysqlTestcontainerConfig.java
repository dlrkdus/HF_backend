package com.hf.healthfriend.testutil;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.util.Map;

@TestConfiguration
@Slf4j
public class MysqlTestcontainerConfig {
    private GenericContainer<?> mysqlContainer;

    @PostConstruct
    public void init() {
        this.mysqlContainer = new GenericContainer<>("mysql:8.0.40")
                .withExposedPorts(3306)
                .withEnv(Map.of(
                        "MYSQL_ROOT_PASSWORD", "1111",
                        "MYSQL_DATABASE", "hf"
                ));
    }

    @PreDestroy
    public void destroy() {
        this.mysqlContainer.stop();
        this.mysqlContainer.close();
        this.mysqlContainer = null;
    }

    @Primary
    @Bean
    public DataSource testDataSource() {
        this.mysqlContainer.start();
        Integer mappedPort = this.mysqlContainer.getMappedPort(3306);
        log.info("MySQL mappedPort={}", mappedPort);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(
                String.format(
                        "jdbc:mysql://%s:%d/hf",
                        this.mysqlContainer.getHost(),
                        this.mysqlContainer.getMappedPort(3306)
                )
        );
        dataSource.setUsername("root");
        dataSource.setPassword("1111");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }
}
