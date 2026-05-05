package hu.brendonkomodi.mythologyslots.service;

import hu.brendonkomodi.mythologyslots.domain.Card;
import hu.brendonkomodi.mythologyslots.repository.CardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> findAll() {
        log.info("Card list is requested");
        return cardRepository.findAll();
    }

    public Card findById(Long id) {
        log.info("Card is requested with id: {}", id);
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
    }
}