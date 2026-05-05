package hu.brendonkomodi.mythologyslots.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Column(nullable = false)
    private String mythologyOrigin;

    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Integer coinPerSecond;

    @Column(nullable = false)
    private boolean symbol = false;
}