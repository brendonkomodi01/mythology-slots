package hu.brendonkomodi.mythologyslots.config;

import hu.brendonkomodi.mythologyslots.domain.Card;
import hu.brendonkomodi.mythologyslots.domain.Rarity;
import hu.brendonkomodi.mythologyslots.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CardRepository cardRepository;

    @Override
    public void run(String... args) {
        if (cardRepository.count() > 0) return;

        cardRepository.saveAll(List.of(
                card("WILD", Rarity.COMMON, true, "GREEK", null),
                card("SCATTER", Rarity.COMMON, true, "GREEK", null),
                card("Zeus", Rarity.LEGENDARY, false, "GREEK", "assets/images/zeus.png"),
                card("Medusa", Rarity.EPIC, false, "GREEK", "assets/images/medusa.png"),
                card("Minotaur", Rarity.RARE, false, "GREEK", "assets/images/minotaur.png"),
                card("Hermes", Rarity.RARE, false, "GREEK", "assets/images/hermes.png"),
                card("Anubis", Rarity.EPIC, false, "EGYPTIAN", "assets/images/anubis.png"),
                card("Ra", Rarity.LEGENDARY, false, "EGYPTIAN", "assets/images/ra.png"),
                card("Fenrir", Rarity.RARE, false, "NORSE", "assets/images/fenrir.png"),
                card("Hydra", Rarity.RARE, false, "GREEK", "assets/images/hydra.png"),
                card("Osiris", Rarity.EPIC, false, "EGYPTIAN", "assets/images/osiris.png"),
                card("Odin", Rarity.LEGENDARY, false, "NORSE", "assets/images/odin.png"),
                card("Athena", Rarity.EPIC, false, "GREEK", "assets/images/athena.png"),
                card("Cerberus", Rarity.RARE, false, "GREEK", "assets/images/cerberus.png"),
                card("Horus", Rarity.EPIC, false, "EGYPTIAN", "assets/images/horus.png"),
                card("Valkyrie", Rarity.RARE, false, "NORSE", "assets/images/valkyrie.png"),
                card("Sphinx", Rarity.RARE, false, "EGYPTIAN", "assets/images/sphinx.png")
        ));
    }

    private Card card(String name, Rarity rarity, boolean symbol, String origin, String imageUrl) {
        Card c = new Card();
        c.setName(name);
        c.setRarity(rarity);
        c.setSymbol(symbol);
        c.setMythologyOrigin(origin);
        c.setCoinPerSecond(0);
        c.setImageUrl(imageUrl);
        return c;
    }
}