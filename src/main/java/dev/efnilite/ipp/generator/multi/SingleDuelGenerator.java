package dev.efnilite.ipp.generator.multi;

import dev.efnilite.ip.ParkourOption;
import dev.efnilite.ip.generator.base.GeneratorOption;
import dev.efnilite.ip.menu.SettingsMenu;
import dev.efnilite.ip.player.ParkourPlayer;
import dev.efnilite.ipp.generator.single.PlusGenerator;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public class SingleDuelGenerator extends PlusGenerator {

    private DuelGenerator owningGenerator;
    private int playerIndex;

    public SingleDuelGenerator(@NotNull ParkourPlayer player, GeneratorOption... generatorOptions) {
        super(player.getSession(), generatorOptions);

        // setup menu
        menu = new SettingsMenu(ParkourOption.SCHEMATICS, ParkourOption.SCORE_DIFFICULTY, ParkourOption.STYLES);
    }

    public void setOwningGenerator(DuelGenerator owningGenerator) {
        this.owningGenerator = owningGenerator;
    }

    public DuelGenerator getOwningGenerator() {
        return owningGenerator;
    }

    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    @Override
    public BlockData selectBlockData() {
        return switch (playerIndex) {
            case 0 -> Material.BLUE_CONCRETE.createBlockData();
            case 1 -> Material.RED_CONCRETE.createBlockData();
            case 2 -> Material.GREEN_CONCRETE.createBlockData();
            case 3 -> Material.YELLOW_CONCRETE.createBlockData();
            default -> Material.STONE.createBlockData();
        };
    }

    public void stopGenerator() {
        this.stopped = true;
    }

    @Override
    public void score() {
        this.score++;
        this.totalScore++;
    }
}
