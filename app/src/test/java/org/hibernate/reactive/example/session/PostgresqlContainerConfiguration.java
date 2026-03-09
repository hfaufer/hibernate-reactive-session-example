package org.hibernate.reactive.example.session;

import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class PostgresqlContainerConfiguration {

    public PostgreSQLContainer<?> postgreSQLContainer() {
        final var postgres = new PostgreSQLContainer<>("postgres:18.3-alpine3.23")
                .withDatabaseName("hr_demo")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.setPortBindings(List.of("5439:5432"));
        return postgres;
    }

}
