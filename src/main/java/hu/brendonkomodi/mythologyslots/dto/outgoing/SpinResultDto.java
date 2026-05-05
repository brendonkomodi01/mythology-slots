package hu.brendonkomodi.mythologyslots.dto.outgoing;

import hu.brendonkomodi.mythologyslots.domain.Card;

import java.util.List;
import java.util.Map;

public class SpinResultDto {

    private List<List<Card>> grid;
    private long coinWon;
    private List<int[]> winningPositions;
    private List<Integer> expandingCols;
    private Map<Integer, String> expandingCardNames;
    private int freeSpins;

    public SpinResultDto(List<List<Card>> grid, long coinWon,
                         List<int[]> winningPositions, List<Integer> expandingCols,
                         Map<Integer, String> expandingCardNames, int freeSpins) {
        this.grid = grid;
        this.coinWon = coinWon;
        this.winningPositions = winningPositions;
        this.expandingCols = expandingCols;
        this.expandingCardNames = expandingCardNames;
        this.freeSpins = freeSpins;
    }

    public List<List<Card>> getGrid() { return grid; }
    public long getCoinWon() { return coinWon; }
    public List<int[]> getWinningPositions() { return winningPositions; }
    public List<Integer> getExpandingCols() { return expandingCols; }
    public Map<Integer, String> getExpandingCardNames() { return expandingCardNames; }
    public int getFreeSpins() { return freeSpins; }
}