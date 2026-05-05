package hu.brendonkomodi.mythologyslots.service;

import hu.brendonkomodi.mythologyslots.domain.AppUser;
import hu.brendonkomodi.mythologyslots.domain.Card;
import hu.brendonkomodi.mythologyslots.domain.Rarity;
import hu.brendonkomodi.mythologyslots.domain.SpinResult;
import hu.brendonkomodi.mythologyslots.dto.outgoing.SpinResultDto;
import hu.brendonkomodi.mythologyslots.repository.CardRepository;
import hu.brendonkomodi.mythologyslots.repository.SpinResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class SlotService {

    private final CardRepository cardRepository;
    private final SpinResultRepository spinResultRepository;
    private final AppUserService appUserService;

    public SlotService(CardRepository cardRepository,
                       SpinResultRepository spinResultRepository,
                       AppUserService appUserService) {
        this.cardRepository = cardRepository;
        this.spinResultRepository = spinResultRepository;
        this.appUserService = appUserService;
    }

    public SpinResultDto spin(String username, long bet, List<Map<String, Integer>> stickyWilds) {
        log.info("Spin requested by user: {} with bet: {}", username, bet);
        AppUser appUser = appUserService.findByUsername(username);

        long effectiveBet = Math.abs(bet);
        boolean isFreeSpin = bet < 0;

        if (effectiveBet > 0 && !isFreeSpin && appUser.getCoinBalance() < effectiveBet) {
            throw new RuntimeException("Not enough coins to spin");
        }

        if (effectiveBet > 0 && !isFreeSpin) {
            appUser.setCoinBalance(appUser.getCoinBalance() - effectiveBet);
            appUserService.save(appUser);
        }

        List<Card> allCards = cardRepository.findAll();
        List<List<Card>> grid = buildGrid(allCards, stickyWilds);

        double totalMultiplier = 0;
        List<int[]> winningPositions = new ArrayList<>();
        List<Integer> expandingCols = new ArrayList<>();
        Map<Integer, String> expandingCardNames = new HashMap<>();

        totalMultiplier += evaluateRows(grid, winningPositions);
        totalMultiplier += evaluateColumns(grid, winningPositions, expandingCols, expandingCardNames);
        totalMultiplier += evaluateDiagonals(grid, winningPositions);

        int scatterCount = countScatters(grid);
        int freeSpins = 0;
        if (scatterCount >= 5) freeSpins = 20;
        else if (scatterCount >= 4) freeSpins = 10;
        log.info("SCATTER count: {}, freeSpins: {}", scatterCount, freeSpins);

        long coinWon = Math.round(effectiveBet * totalMultiplier);
        if (coinWon > 0) {
            appUser.setCoinBalance(appUser.getCoinBalance() + coinWon);
            appUserService.save(appUser);
        }

        SpinResult spinResult = new SpinResult();
        spinResult.setAppUser(appUser);
        spinResult.setCoinsSpent(effectiveBet);
        spinResult.setSpunAt(LocalDateTime.now());
        spinResultRepository.save(spinResult);

        log.info("Spin completed for user: {}, multiplier: {}x, coinWon: {}, isFreeSpin: {}", username, totalMultiplier, coinWon, isFreeSpin);
        return new SpinResultDto(grid, coinWon, winningPositions, expandingCols, expandingCardNames, freeSpins);
    }

    private List<List<Card>> buildGrid(List<Card> allCards, List<Map<String, Integer>> stickyWilds) {
        List<List<Card>> grid = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            List<Card> rowCards = new ArrayList<>();
            for (int col = 0; col < 5; col++) rowCards.add(null);
            grid.add(rowCards);
        }

        boolean[] scatterUsedInCol = new boolean[5];
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Card card = getRandomCard(allCards);
                if (card != null && card.isSymbol() && "SCATTER".equals(card.getName())) {
                    if (scatterUsedInCol[col]) {
                        card = getNonScatterCard(allCards);
                    } else {
                        scatterUsedInCol[col] = true;
                    }
                }
                grid.get(row).set(col, card);
            }
        }

        Card wildCard = allCards.stream()
                .filter(c -> c.isSymbol() && "WILD".equals(c.getName()))
                .findFirst().orElse(null);

        if (wildCard != null && stickyWilds != null) {
            for (Map<String, Integer> sticky : stickyWilds) {
                int row = sticky.get("row");
                int col = sticky.get("col");
                if (row >= 0 && row < 5 && col >= 0 && col < 5) {
                    grid.get(row).set(col, wildCard);
                    log.info("Sticky wild placed at row: {}, col: {}", row, col);
                }
            }
        }

        return grid;
    }

    private double evaluateRows(List<List<Card>> grid, List<int[]> winningPositions) {
        double total = 0;
        for (int row = 0; row < 5; row++) {
            List<Card> rowCards = grid.get(row);
            if (allSameWithWild(rowCards, 0, 4)) {
                Card base = getNonWildCard(rowCards, 0, 4);
                total += base != null ? getCardMultiplier5(base) : 1.0;
                for (int c = 0; c <= 4; c++) winningPositions.add(new int[]{row, c});
            } else if (allSameWithWild(rowCards, 0, 3)) {
                Card base = getNonWildCard(rowCards, 0, 3);
                total += base != null ? getCardMultiplier4(base) : 1.0;
                for (int c = 0; c <= 3; c++) winningPositions.add(new int[]{row, c});
            } else if (allSameWithWild(rowCards, 1, 4)) {
                Card base = getNonWildCard(rowCards, 1, 4);
                total += base != null ? getCardMultiplier4(base) : 1.0;
                for (int c = 1; c <= 4; c++) winningPositions.add(new int[]{row, c});
            } else {
                for (int col = 0; col <= 2; col++) {
                    if (allSameWithWild(rowCards, col, col + 2)) {
                        Card base = getNonWildCard(rowCards, col, col + 2);
                        total += base != null ? getCardMultiplier(base) : 1.0;
                        for (int c = col; c <= col + 2; c++) winningPositions.add(new int[]{row, c});
                        break;
                    }
                }
            }
        }
        return total;
    }

    private double evaluateColumns(List<List<Card>> grid, List<int[]> winningPositions,
                                   List<Integer> expandingCols, Map<Integer, String> expandingCardNames) {
        double total = 0;
        for (int col = 0; col < 5; col++) {
            List<Card> column = new ArrayList<>();
            for (int row = 0; row < 5; row++) column.add(grid.get(row).get(col));
            log.info("Column {}: {}", col, column.stream().map(Card::getName).toList());

            if (allSameWithWild(column, 0, 4)) {
                Card base = getNonWildCard(column, 0, 4);
                total += base != null ? getCardMultiplier5(base) : 1.0;
                for (int r = 0; r <= 4; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            } else if (allSameWithWild(column, 0, 3)) {
                Card base = getNonWildCard(column, 0, 3);
                total += base != null ? getCardMultiplier4(base) : 1.0;
                for (int r = 0; r <= 3; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            } else if (allSameWithWild(column, 1, 4)) {
                Card base = getNonWildCard(column, 1, 4);
                total += base != null ? getCardMultiplier4(base) : 1.0;
                for (int r = 1; r <= 4; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            } else if (allSameWithWild(column, 0, 2)) {
                Card base = getNonWildCard(column, 0, 2);
                total += base != null ? getCardMultiplier(base) : 1.0;
                for (int r = 0; r <= 2; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            } else if (allSameWithWild(column, 1, 3)) {
                Card base = getNonWildCard(column, 1, 3);
                total += base != null ? getCardMultiplier(base) : 1.0;
                for (int r = 1; r <= 3; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            } else if (allSameWithWild(column, 2, 4)) {
                Card base = getNonWildCard(column, 2, 4);
                total += base != null ? getCardMultiplier(base) : 1.0;
                for (int r = 2; r <= 4; r++) winningPositions.add(new int[]{r, col});
                expandingCols.add(col);
                if (base != null) expandingCardNames.put(col, base.getName().toLowerCase());
            }
        }
        return total;
    }

    private double evaluateDiagonals(List<List<Card>> grid, List<int[]> winningPositions) {
        double total = 0;

        List<Card> diag1 = List.of(
                grid.get(0).get(0), grid.get(1).get(1),
                grid.get(2).get(2), grid.get(3).get(3), grid.get(4).get(4));

        if (allSameWithWild(diag1, 0, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 0, 4), true, false);
            winningPositions.addAll(List.of(new int[]{0,0}, new int[]{1,1}, new int[]{2,2}, new int[]{3,3}, new int[]{4,4}));
        } else if (allSameWithWild(diag1, 0, 3)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 0, 3), false, true);
            winningPositions.addAll(List.of(new int[]{0,0}, new int[]{1,1}, new int[]{2,2}, new int[]{3,3}));
        } else if (allSameWithWild(diag1, 1, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 1, 4), false, true);
            winningPositions.addAll(List.of(new int[]{1,1}, new int[]{2,2}, new int[]{3,3}, new int[]{4,4}));
        } else if (allSameWithWild(diag1, 0, 2)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 0, 2), false, false);
            winningPositions.addAll(List.of(new int[]{0,0}, new int[]{1,1}, new int[]{2,2}));
        } else if (allSameWithWild(diag1, 1, 3)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 1, 3), false, false);
            winningPositions.addAll(List.of(new int[]{1,1}, new int[]{2,2}, new int[]{3,3}));
        } else if (allSameWithWild(diag1, 2, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag1, 2, 4), false, false);
            winningPositions.addAll(List.of(new int[]{2,2}, new int[]{3,3}, new int[]{4,4}));
        }

        List<Card> diag2 = List.of(
                grid.get(0).get(4), grid.get(1).get(3),
                grid.get(2).get(2), grid.get(3).get(1), grid.get(4).get(0));

        if (allSameWithWild(diag2, 0, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 0, 4), true, false);
            winningPositions.addAll(List.of(new int[]{0,4}, new int[]{1,3}, new int[]{2,2}, new int[]{3,1}, new int[]{4,0}));
        } else if (allSameWithWild(diag2, 0, 3)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 0, 3), false, true);
            winningPositions.addAll(List.of(new int[]{0,4}, new int[]{1,3}, new int[]{2,2}, new int[]{3,1}));
        } else if (allSameWithWild(diag2, 1, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 1, 4), false, true);
            winningPositions.addAll(List.of(new int[]{1,3}, new int[]{2,2}, new int[]{3,1}, new int[]{4,0}));
        } else if (allSameWithWild(diag2, 0, 2)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 0, 2), false, false);
            winningPositions.addAll(List.of(new int[]{0,4}, new int[]{1,3}, new int[]{2,2}));
        } else if (allSameWithWild(diag2, 1, 3)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 1, 3), false, false);
            winningPositions.addAll(List.of(new int[]{1,3}, new int[]{2,2}, new int[]{3,1}));
        } else if (allSameWithWild(diag2, 2, 4)) {
            total += getMultiplierOrDefault(getNonWildCard(diag2, 2, 4), false, false);
            winningPositions.addAll(List.of(new int[]{2,2}, new int[]{3,1}, new int[]{4,0}));
        }

        return total;
    }

    private double getMultiplierOrDefault(Card base, boolean five, boolean four) {
        if (base == null) return 1.0;
        if (five) return getCardMultiplier5(base);
        if (four) return getCardMultiplier4(base);
        return getCardMultiplier(base);
    }

    private int countScatters(List<List<Card>> grid) {
        int count = 0;
        for (int row = 0; row < 5; row++)
            for (int col = 0; col < 5; col++)
                if (grid.get(row).get(col).isSymbol() && "SCATTER".equals(grid.get(row).get(col).getName()))
                    count++;
        return count;
    }

    private boolean isWild(Card card) {
        return card.isSymbol() && "WILD".equals(card.getName());
    }

    private boolean allSameWithWild(List<Card> cards, int from, int to) {
        Long baseId = null;
        for (int i = from; i <= to; i++) {
            Card card = cards.get(i);
            if (isWild(card)) continue;
            if (card.isSymbol() && "SCATTER".equals(card.getName())) return false;
            if (baseId == null) baseId = card.getId();
            else if (!card.getId().equals(baseId)) return false;
        }
        return true;
    }

    private Card getNonWildCard(List<Card> cards, int from, int to) {
        for (int i = from; i <= to; i++)
            if (!isWild(cards.get(i))) return cards.get(i);
        return null;
    }

    private double getCardMultiplier(Card card) {
        if (card.isSymbol()) {
            return switch (card.getName()) {
                case "J" -> 0.5;
                case "Q" -> 0.6;
                case "K" -> 0.7;
                case "A" -> 0.8;
                case "WILD" -> 2.0;
                default -> 0.5;
            };
        }
        return switch (card.getName().toLowerCase()) {
            case "zeus", "medusa" -> 15.0;
            case "minotaur", "hermes" -> 10.0;
            case "anubis", "ra" -> 8.0;
            case "fenrir", "hydra", "osiris", "odin" -> 5.0;
            case "athena", "cerberus", "horus" -> 6.0;
            case "valkyrie", "sphinx" -> 7.0;
            default -> 1.0;
        };
    }

    private double getCardMultiplier4(Card card) {
        if (card.isSymbol()) return getCardMultiplier(card);
        return switch (card.getName().toLowerCase()) {
            case "zeus", "medusa" -> 17.0;
            case "minotaur", "hermes" -> 12.0;
            case "anubis", "ra" -> 10.0;
            case "fenrir", "hydra", "osiris", "odin" -> 7.0;
            case "athena", "cerberus", "horus" -> 8.0;
            case "valkyrie", "sphinx" -> 9.0;
            default -> 1.0;
        };
    }

    private double getCardMultiplier5(Card card) {
        if (card.isSymbol()) return getCardMultiplier(card);
        return switch (card.getName().toLowerCase()) {
            case "zeus", "medusa" -> 20.0;
            case "minotaur", "hermes" -> 15.0;
            case "anubis", "ra" -> 13.0;
            case "fenrir", "hydra", "osiris", "odin" -> 10.0;
            case "athena", "cerberus", "horus" -> 11.0;
            case "valkyrie", "sphinx" -> 12.0;
            default -> 1.0;
        };
    }

    private Card getRandomCard(List<Card> allCards) {
        if (new Random().nextInt(100) < 6) {
            return allCards.stream()
                    .filter(c -> c.isSymbol() && "SCATTER".equals(c.getName()))
                    .findFirst().orElse(null);
        }
        if (new Random().nextInt(100) < 4) {
            return allCards.stream()
                    .filter(c -> c.isSymbol() && "WILD".equals(c.getName()))
                    .findFirst().orElse(null);
        }
        if (new Random().nextInt(100) < 15) {
            List<Card> symbols = allCards.stream()
                    .filter(c -> c.isSymbol() && !"SCATTER".equals(c.getName()) && !"WILD".equals(c.getName()))
                    .toList();
            if (!symbols.isEmpty()) return symbols.get(new Random().nextInt(symbols.size()));
        }
        return getRandomNonSymbolCard(allCards);
    }

    private Card getNonScatterCard(List<Card> allCards) {
        if (new Random().nextInt(100) < 3) {
            return allCards.stream()
                    .filter(c -> c.isSymbol() && "WILD".equals(c.getName()))
                    .findFirst().orElse(null);
        }
        if (new Random().nextInt(100) < 20) {
            List<Card> symbols = allCards.stream()
                    .filter(c -> c.isSymbol() && !"SCATTER".equals(c.getName()) && !"WILD".equals(c.getName()))
                    .toList();
            if (!symbols.isEmpty()) return symbols.get(new Random().nextInt(symbols.size()));
        }
        return getRandomNonSymbolCard(allCards);
    }

    private Card getRandomNonSymbolCard(List<Card> allCards) {
        Rarity rarity = getRandomRarity();
        List<Card> filtered = allCards.stream()
                .filter(c -> !c.isSymbol() && c.getRarity() == rarity)
                .toList();
        if (filtered.isEmpty()) {
            List<Card> nonSymbols = allCards.stream().filter(c -> !c.isSymbol()).toList();
            return nonSymbols.get(new Random().nextInt(nonSymbols.size()));
        }
        return filtered.get(new Random().nextInt(filtered.size()));
    }

    private Rarity getRandomRarity() {
        int roll = new Random().nextInt(100);
        if (roll < 15) return Rarity.COMMON;
        if (roll < 35) return Rarity.UNCOMMON;
        if (roll < 55) return Rarity.RARE;
        if (roll < 75) return Rarity.EPIC;
        if (roll < 90) return Rarity.LEGENDARY;
        return Rarity.MYTHIC;
    }
}