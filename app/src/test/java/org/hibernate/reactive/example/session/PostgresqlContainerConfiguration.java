package org.hibernate.reactive.example.session;

import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class PostgresqlContainerConfiguration {

    public PostgreSQLContainer<?> postgreSQLContainer() {
        final var postgres = new PostgreSQLContainer<>("postgres:18.3-alpine3.23")
                .withDatabaseName("hreact")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.setPortBindings(List.of("18432:5432"));
        return postgres;
    }

}
