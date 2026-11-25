package com.example.baitapsession12.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplies")
@Getter
@Setter
@NoArgsConstructor
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String specification;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false)
    private long quantity;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
