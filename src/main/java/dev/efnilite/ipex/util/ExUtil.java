package dev.efnilite.ipex.util;

import dev.efnilite.witp.generator.subarea.Direction;
import dev.efnilite.witp.schematic.Vector3D;
import dev.efnilite.witp.util.Logging;
import dev.efnilite.witp.util.Util;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ExUtil {

    // includes base
    public static List<Block> getBlocksAround(Block base, int radius) {
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
    public static Vector3D getDirectionVector(Direction direction) {
        switch (direction) {
            case NORTH:
                return new Vector3D(0, 0, -1);
            case SOUTH:
                return new Vector3D(0, 0, 1);
            case EAST:
                return new Vector3D(1, 0, 0);
            case WEST:
                return new Vector3D(-1, 0, 0);
            default:
                Logging.error("Invalid direction (direction used: " + direction.name() + ")");
                return new Vector3D(1, 0, 0);
        }
    }
}