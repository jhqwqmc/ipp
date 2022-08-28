package dev.efnilite.ipp.generator.single;

import dev.efnilite.ip.ParkourOption;
import dev.efnilite.ip.api.Gamemode;
import dev.efnilite.ip.api.events.PlayerScoreEvent;
import dev.efnilite.ip.generator.settings.GeneratorOption;
import dev.efnilite.ip.menu.settings.ParkourSettingsMenu;
import dev.efnilite.ip.session.Session;
import dev.efnilite.ip.util.Util;
import dev.efnilite.ipp.gamemode.PlusGamemodes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class for speed jump gamemode
 */
public final class SuperJumpGenerator extends PlusGenerator {

    private double jumpDistance = 3;
    private final LinkedHashMap<List<Block>, Integer> positionIndexMap = new LinkedHashMap<>();

    public SuperJumpGenerator(Session session) {
        // setup settings
        super(session, GeneratorOption.DISABLE_SCHEMATICS, GeneratorOption.DISABLE_SPECIAL, GeneratorOption.DISABLE_ADAPTIVE, GeneratorOption.REDUCE_RANDOM_BLOCK_SELECTION_ANGLE);

        // setup menu
        menu = new ParkourSettingsMenu(ParkourOption.LEADS, ParkourOption.SCHEMATIC,
                ParkourOption.SCORE_DIFFICULTY, ParkourOption.SPECIAL_BLOCKS);

        updateJumpDistance();

        heightChances.clear();
        heightChances.put(0, 0);

        player.player.setMaximumAir(100_000_000);
    }

    @Override
    public void updatePreferences() {
        profile.setSetting("blockLead", "1");
    }

    // update jump distance
    private void updateJumpDistance() {
        jumpDistance += 0.3;

        distanceChances.clear();
        distanceChances.put(0, (int) jumpDistance);

        Player bPlayer = player.player;

        // According to gathered data, the potion effect line goes roughly like this:
        // y = 3.93x - 34.9, where x is the jump distance
        // however players should start with speed 2
        // to make every jump doable shift the line left
        // level = 3.93 * distance - 31
        double level = 3.93 * jumpDistance - 31;
        if (level < 4) {
            level = 1.5 * jumpDistance; // in the beginning
        } else if (level > 255) {
            level = 255;
        }

        if (level < 255) { // player has potion and new level will be under 255
            bPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, (int) level, false, false));
        }
    }

    @Override
    public List<Block> selectBlocks() {
        Block next = selectNext(mostRecentBlock, (int) jumpDistance, 0);// no difference in height

        if (next == null) {
            return Collections.emptyList();
        }

        List<Block> blocks = getBlocksAround(next, 1);
        mostRecentBlock = next.getLocation();

        return blocks;
    }

    private List<Block> getBlocksAround(Block base, int radius) {
        int lastOfRadius = 2 * radius + 1;
        int baseX = base.getX();
        int baseY = base.getY();
        int baseZ = base.getZ();

        List<Block> blocks = new ArrayList<>();
        World world = base.getWorld();
        int amount = lastOfRadius * lastOfRadius;
        for (int i = 0; i < amount; i++) {
            int[] coords = Util.spiralAt(i);
            int x = coords[0];
            int z = coords[1];

            x += baseX;
            z += baseZ;

            blocks.add(world.getBlockAt(x, baseY, z));
        }
        return blocks;
    }

    /**
     * Starts the check
     */
    @Override
    public void tick() {
        updateTime();
        updateScoreboard();

        player.getSession().updateSpectators();
        player.player.setSaturation(20);

        Location playerLocation = player.getLocation();

        if (playerLocation.getWorld() != playerSpawn.getWorld()) { // sometimes player worlds don't match (somehow)
            player.teleport(playerSpawn);
            return;
        }

        if (lastStandingPlayerLocation.getY() - playerLocation.getY() > 10 && playerSpawn.distance(playerLocation) > 5) { // Fall check
            fall();
            return;
        }

        Block blockBelowPlayer = playerLocation.clone().subtract(0, 1, 0).getBlock(); // Get the block below

        if (blockBelowPlayer.getType() == Material.AIR) {
            return;
        }

        List<Block> platform = match(blockBelowPlayer);
        if (platform == null) {
            return;
        }
        int currentIndex = positionIndexMap.get(platform); // current index of the player
        int deltaFromLast = currentIndex - lastPositionIndexPlayer;

        if (deltaFromLast <= 0 && score > 0) { // the player is actually making progress and not going backwards (current index is higher than the previous)
            return;
        }

        if (!stopwatch.hasStarted()) { // start stopwatch when first point is achieved
            stopwatch.start();
        }

        lastStandingPlayerLocation = playerLocation.clone();

        new PlayerScoreEvent(player).call();
        score();

        int blockLead = profile.getValue("blockLead").asInt();
        int deltaCurrentTotal = positionIndexTotal - currentIndex; // delta between current index and total
        if (deltaCurrentTotal <= blockLead) {
            generate(blockLead - deltaCurrentTotal); // generate the remaining amount so it will match
        }
        lastPositionIndexPlayer = currentIndex;

        // delete trailing blocks
        for (List<Block> blocks : new ArrayList<>(positionIndexMap.keySet())) {
            int index = positionIndexMap.get(blocks);
            if (currentIndex - index > 2) {
                blocks.forEach(block -> block.setType(Material.AIR));

                positionIndexMap.remove(blocks);
            }
        }
    }

    private @Nullable List<Block> match(Block current) {
        for (List<Block> blocks : positionIndexMap.keySet()) {
            if (blocks.contains(current)) {
                return blocks;
            }
        }
        return null;
    }

    @Override
    public void generate() {
        List<Block> blocks = selectBlocks();

        positionIndexMap.put(blocks, positionIndexTotal);
        for (Block block : blocks) {
            setBlock(block, selectBlockData());
        }
        particles(blocks);
        positionIndexTotal++;
    }

    @Override
    public void reset(boolean regenerate) {
        jumpDistance = 3;

        player.player.removePotionEffect(PotionEffectType.SPEED);
        if (regenerate) {
            updateJumpDistance();
        }

        // clear history
        for (List<Block> blocks : positionIndexMap.keySet()) {
            blocks.forEach(block -> block.setType(Material.AIR));
        }
        positionIndexMap.clear();

        super.reset(regenerate);
    }

    @Override
    public void generate(int amount) {
        generate();
    }

    @Override
    public void score() {
        super.score();

        updateJumpDistance();
    }

    @Override
    public Gamemode getGamemode() {
        return PlusGamemodes.SUPER_JUMP;
    }
}