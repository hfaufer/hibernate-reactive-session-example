/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive.example.session;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Book extends BaseEntity {
    @Size(min = 13, max = 13)
    private String isbn;
    @NotNull
    @Size(max = 100)
    private String title;
    @Basic(fetch = LAZY)
    @NotNull
    @Past
    private LocalDate published;
    @Basic(fetch = LAZY)
    public byte[] coverImage;
    @NotNull
    @ManyToOne(fetch = LAZY)
    private Author author;

    public Book() {
    }

    public Book(final String isbn, final String title, final Author author, final LocalDate published) {
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.published = published;
        this.coverImage = ("Cover image for '" + title + "'").getBytes();
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public LocalDate getPublished() {
        return published;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }
}
