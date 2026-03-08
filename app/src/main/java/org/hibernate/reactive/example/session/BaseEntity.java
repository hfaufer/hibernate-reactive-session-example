package org.hibernate.reactive.example.session;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Version
    private Integer version;

    public Integer getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }
}
