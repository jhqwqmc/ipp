package dev.efnilite.ipp.generator;

import dev.efnilite.ip.generator.DefaultGenerator;
import dev.efnilite.ip.generator.base.GeneratorOption;
import dev.efnilite.ip.session.Session;
import dev.efnilite.vilib.chat.Message;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.time.Duration;
import java.time.Instant;

/**
 * Class for only having 1 second to stay on the block
 */
public final class HourglassGenerator extends DefaultGenerator {

    private Block last = null;
    private Instant instant = Instant.now();

    public HourglassGenerator(Session session) {
        super(session, GeneratorOption.DISABLE_SCHEMATICS);
    }

    @Override
    public void tick() {
        super.tick();

        if (score == 0 || last == null) {
            return;
        }

        if (lastPositionIndexPlayer == positionIndexMap.get(last)) {
            instant = Instant.now();
        }

        Location loc = player.getLocation();
        Block at = loc.getBlock();
        Block current = loc.clone().subtract(0, 1, 0).getBlock();
        if (at.getType() != Material.AIR) {
            current = at;
        }

        Duration delta = Duration.between(instant, Instant.now());
        StringBuilder bar = new StringBuilder(); // build bar with time remaining
        bar.append("<green><bold>");
        int time = (int) (delta.toMillis() / 100) - 1;
        if (current.getType() == Material.AIR) {
            time = 0;
        }
        for (int i = 0; i < 10; i++) {
            if (i == time) {
                bar.append("<dark_gray><bold>");
            }
            bar.append("|||");
        }

        player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Message.parseFormatting(bar.toString())));

        if (delta.getSeconds() < 1) {
            return;
        }

        last.setType(Material.AIR);
    }

    @Override
    public void score() {
        super.score();

        instant = Instant.now();

        Location loc = player.getLocation();
        Block at = loc.getBlock();
        this.last = loc.clone().subtract(0, 1, 0).getBlock();
        if (at.getType() != Material.AIR) {
            this.last = at.getLocation().getBlock();
        }
    }
}
