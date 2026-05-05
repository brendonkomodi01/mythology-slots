package hu.brendonkomodi.mythologyslots.repository;

import hu.brendonkomodi.mythologyslots.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}