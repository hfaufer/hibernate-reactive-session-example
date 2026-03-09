package org.hibernate.reactive.example.session;

import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompletionStageTest {
    private static final String DEFAULT_PERSISTENCE_UNIT = "postgresql-example";
    private static PostgreSQLContainer postgreSQLContainer;

    private Stage.SessionFactory factory;
    final Author author1 = new Author("Iain M. Banks");
    final Author author2 = new Author("Neal Stephenson");
    final Book book1 = new Book("1-85723-235-6", "Feersum Endjinn", author1, LocalDate.of(1994, JANUARY, 1));
    final Book book2 = new Book("0-380-97346-4", "Cryptonomicon", author2, LocalDate.of(1999, MAY, 1));
    final Book book3 = new Book("0-553-08853-X", "Snow Crash", author2, LocalDate.of(1992, JUNE, 1));

    @BeforeAll
    static void startPostgresqlContainer() {
        postgreSQLContainer = new PostgresqlContainerConfiguration().postgreSQLContainer();
        postgreSQLContainer.start();
    }

    @AfterAll
    static void stopPostgresqlContainer() {
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void setUp() {
        factory = createEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT).unwrap(Stage.SessionFactory.class);
        author1.getBooks().add(book1);
        author2.getBooks().addAll(List.of(book2, book3));
    }

    @AfterEach
    void tearDown() {
        author1.getBooks().clear();
        author2.getBooks().clear();
        factory.close();
    }

    @Test
    void persist_author_also_persists_the_books() {
        factory.withTransaction((session, _) -> session.persist(author2))
                .toCompletableFuture().join();

        List.of(book2, book3).forEach(book -> {
            final Book foundBook = factory.withSession(session -> session.find(Book.class, book.getId()))
                    .toCompletableFuture().join();
            assertNotNull(foundBook);
            assertEquals(book.getTitle(), foundBook.getTitle());
        });
    }

}
