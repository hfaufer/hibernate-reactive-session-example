/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive.example.session;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;

import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static java.lang.System.out;
import static java.time.Month.*;
import static org.hibernate.reactive.stage.Stage.SessionFactory;
import static org.hibernate.reactive.stage.Stage.fetch;

/**
 * Demonstrates the use of Hibernate Reactive with the {@link java.util.concurrent.CompletionStage}-based API.
 */
public class CompletionStageMain {

    private static final String DEFAULT_PERSISTENCE_UNIT = "postgresql-example";

    // The first argument can be used to select a persistence unit. Check resources/META-INF/persistence.xml for
    // available names.
    void main(final String[] args) {
        IO.println("== CompletionStage API Example ==");

        // Define some test data.
        final Author author1 = new Author("Iain M. Banks");
        final Author author2 = new Author("Neal Stephenson");
        final Book book1 = new Book("1-85723-235-6", "Feersum Endjinn", author1, LocalDate.of(1994, JANUARY, 1));
        final Book book2 = new Book("0-380-97346-4", "Cryptonomicon", author2, LocalDate.of(1999, MAY, 1));
        final Book book3 = new Book("0-553-08853-X", "Snow Crash", author2, LocalDate.of(1992, JUNE, 1));
        author1.getBooks().add(book1);
        author2.getBooks().add(book2);
        author2.getBooks().add(book3);

        // Obtain a factory for reactive sessions based on the standard JPA configuration properties specified in
        // resources/META-INF/persistence.xml
        try (SessionFactory factory = createEntityManagerFactory(persistenceUnitName(args))
                .unwrap(SessionFactory.class)) {
            // Obtain a reactive session.
            factory.withTransaction(
                            // Persist the Authors with their Books in a transaction.
                            (session, _) -> session.persist(author1, author2)
                    )
                    // Wait for it to finish.
                    .toCompletableFuture().join();

            factory.withSession(
                            // Retrieve a Book.
                            session -> session.find(Book.class, book1.getId())
                                    // Print its title.
                                    .thenAccept(book -> IO.println(book.getTitle() + " is a great book!"))
                    )
                    .toCompletableFuture().join();

            try {
                factory.withSession(
                                // Retrieve an Author.
                                session -> session.find(Author.class, author1.getId())
                                        // Print her/his name.
                                        .thenApply(author -> {
                                            IO.println(author.getName() + " is a great author!");
                                            return author;
                                        })
                                        // Then print her/his books without explicitly fetching them first.
                                        // This causes the following exception:
                                        // "initialized: org.hibernate.reactive.example.session.Author.books
                                        //   Fetch the collection using 'Mutiny.fetch', 'Stage.fetch', or 'fetch join' in HQL"
                                        .thenAccept(author -> author.getBooks().forEach(book -> IO.println(" - " + book.getTitle())))
                        )
                        .toCompletableFuture().join();
            } catch (Exception e) {
                IO.println("Exception: " + e.getMessage());
            }

            factory.withSession(
                            // Retrieve both Authors at once.
                            session -> session.find(Author.class, author1.getId(), author2.getId())
                                    .thenAccept(authors -> authors.forEach(author -> IO.println(author.getName())))
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Retrieve an Author.
                            session -> session.find(Author.class, author2.getId())
                                    // Lazily fetch their books.
                                    .thenCompose(author -> fetch(author.getBooks())
                                            // Print some info.
                                            .thenAccept(books -> {
                                                IO.println(author.getName() + " wrote " + books.size() + " books");
                                                books.forEach(book -> IO.println(book.getTitle()));
                                            })
                                    )
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Retrieve the Author lazily from a Book.
                            session -> session.find(Book.class, book1.getId())
                                    // Fetch a lazy field of the Book
                                    .thenCompose(book -> fetch(book.getAuthor())
                                            // Print the lazy field.
                                            .thenAccept(author -> out.printf("%s wrote '%s'\n", author.getName(), book1.getTitle()))
                                    )
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Query the Book titles.
                            session -> session.createQuery(
                                            "SELECT title, author.name FROM Book ORDER BY title DESC",
                                            Object[].class
                                    )
                                    .getResultList()
                                    .thenAccept(rows -> rows.forEach(row -> out.printf("%s (%s)\n", row[0], row[1])))
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Retrieve an Author by name.
                            session -> session.createQuery("FROM Author WHERE name = :name", Author.class)
                                    .setParameter("name", "Neal Stephenson")
                                    .getSingleResult()
                                    .thenAccept(author -> {
                                        IO.println(author.getName() + " has ID " + author.getId());
                                    })
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Query the entire Book entities.
                            session -> session.createQuery(
                                            "FROM Book book JOIN FETCH book.author ORDER BY book.title DESC",
                                            Book.class
                                    )
                                    .getResultList()
                                    .thenAccept(books -> books.forEach(
                                            b -> out.printf(
                                                    "%s: %s (%s)\n",
                                                    b.getIsbn(),
                                                    b.getTitle(),
                                                    b.getAuthor().getName()
                                            )
                                    ))
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Use a criteria query.
                            session -> {
                                final CriteriaQuery<Book> query = factory.getCriteriaBuilder().createQuery(Book.class);
                                final Root<Author> a = query.from(Author.class);
                                final Join<Author, Book> b = a.join(Author_.books);
                                query.where(a.get(Author_.name).in("Neal Stephenson", "William Gibson"));
                                query.select(b);
                                return session.createQuery(query)
                                        .getResultList()
                                        .thenAccept(books -> books.forEach(book -> IO.println(book.getTitle())));
                            }
                    )
                    .toCompletableFuture().join();

            factory.withSession(
                            // Retrieve a Book.
                            session -> session.find(Book.class, book1.getId())
                                    // Fetch a lazy field of the Book.
                                    .thenCompose(book -> session.fetch(book, Book_.published)
                                            // Print the lazy field.
                                            .thenAccept(published -> out.printf(
                                                    "'%s' was published in %d\n",
                                                    book.getTitle(),
                                                    published.getYear()
                                            ))
                                    )
                    )
                    .toCompletableFuture().join();

            factory.withTransaction(
                            // Retrieve a Book.
                            (session, _) -> session.find(Book.class, book2.getId())
                                    // Delete the Book.
                                    .thenCompose(session::remove)
                    )
                    .toCompletableFuture().join();

            factory.withTransaction(
                            // Delete all the Books in a transaction.
                            (session, _) -> session.createQuery("DELETE Book").executeUpdate()
                                    // Delete all the Authors.
                                    .thenCompose($ -> session.createQuery("DELETE Author").executeUpdate())
                    )
                    .toCompletableFuture().join();
        }
    }

    /**
     * Return the persistence unit name to use in the example.
     *
     * @param args the first element is the persistence unit name if present
     * @return the selected persistence unit name or the default one
     */
    public static String persistenceUnitName(final String[] args) {
        return args.length > 0 ? args[0] : DEFAULT_PERSISTENCE_UNIT;
    }
}
