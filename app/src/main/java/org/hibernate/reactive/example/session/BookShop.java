package org.hibernate.reactive.example.session;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class BookShop extends BaseEntity{
    private String name;
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "book_shop__book",
            joinColumns = @JoinColumn(name = "book_shop_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books = new ArrayList<>();

    protected BookShop() {
    }

    public BookShop(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }
}
