package dev.efnilite.ipp.generator;

import dev.efnilite.ip.ParkourOption;
import dev.efnilite.ip.generator.DefaultGenerator;
import dev.efnilite.ip.generator.base.GeneratorOption;
import dev.efnilite.ip.menu.SettingsMenu;
import dev.efnilite.ip.session.Session;
import dev.efnilite.vilib.chat.Message;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Fastest to  100 score
 */
public final class TimeTrialGenerator extends DefaultGenerator {

    private final int goal = 100;

    public TimeTrialGenerator(Session session) {
        super(session, GeneratorOption.DISABLE_SCHEMATICS, GeneratorOption.DISABLE_SPECIAL, GeneratorOption.DISABLE_ADAPTIVE);

        player.getPlayer().resetTitle();
    }

    @Override
    public void tick() {
        super.tick();

        // Display score to player
        StringBuilder bar = new StringBuilder(); // build bar with score remaining
        bar.append("&2<bold>");
        for (int i = 0; i < goal; i++) {
            if (i == score) {
                bar.append("&8<bold>");
            }
            if (i % 2 == 0) { // !! made for 100 score
                bar.append("|");
            }
        }
        player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Message.parseFormatting(bar + " <dark_red><bold>| &8" + time)));
    }

    @Override
    public void score() {
        score++;
        totalScore++;

        if (score >= goal) {
            score = goal;
            player.teleport(player.getLocation().subtract(0, 15, 0));
        }
    }

    @Override
    public void menu() {
        SettingsMenu.open(player, ParkourOption.SCHEMATICS, ParkourOption.SCORE_DIFFICULTY, ParkourOption.SPECIAL_BLOCKS);
    }
}