package dev.efnilite.ipp;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.api.Gamemode;
import dev.efnilite.ip.menu.LobbyMenu;
import dev.efnilite.ip.menu.MainMenu;
import dev.efnilite.ip.player.ParkourPlayer;
import dev.efnilite.ip.player.ParkourUser;
import dev.efnilite.ipp.config.PlusConfiguration;
import dev.efnilite.ipp.gamemode.PlusGamemodes;
import dev.efnilite.ipp.gamemode.multi.DuelsGamemode;
import dev.efnilite.ipp.gamemode.multi.TeamSurvivalGamemode;
import dev.efnilite.ipp.gamemode.single.*;
import dev.efnilite.ipp.generator.single.PracticeGenerator;
import dev.efnilite.ipp.menu.ActiveMenu;
import dev.efnilite.ipp.menu.InviteMenu;
import dev.efnilite.ipp.menu.MultiplayerMenu;
import dev.efnilite.ipp.mode.LobbyMode;
import dev.efnilite.ipp.session.MultiSession;
import dev.efnilite.ipp.style.IncrementalStyle;
import dev.efnilite.vilib.ViPlugin;
import dev.efnilite.vilib.inventory.item.Item;
import dev.efnilite.vilib.util.Logging;
import dev.efnilite.vilib.util.Time;
import dev.efnilite.vilib.util.elevator.GitElevator;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public final class IPP extends ViPlugin {

    private static IPP instance;
    private static PlusConfiguration configuration;

    @Override
    public void enable() {
        instance = this;
        Time.timerStart("enable");

        configuration = new PlusConfiguration(this);
        dev.efnilite.ipp.config.PlusOption.init();

        // Events
        registerListener(new PlusHandler());
        registerCommand("ipp", new PlusCommand());

        // Gamemode register
        registerGamemode(new PracticeGamemode());
        registerGamemode(new TeamSurvivalGamemode());
        registerGamemode(new LobbyGamemode());
        registerGamemode(new SpeedGamemode());
        registerGamemode(new SpeedJumpGamemode());
        registerGamemode(new HourglassGamemode());
        registerGamemode(new TimeTrialGamemode());
        registerGamemode(new DuelsGamemode());

        // Style register
        IP.getRegistry().registerType(new IncrementalStyle());
        IP.getRegistry().getStyleType("incremental").addConfigStyles("styles.incremental.list", configuration.getFile("config"));

        LobbyMode.read();
        PlusGamemodes.init();

        // Register stuff for main menu
        // Multiplayer if player is not found
        MainMenu.INSTANCE.registerMainItem(1, 1,
                user -> new Item(Material.OAK_BOAT, "<#0088CB><bold>Multiplayer").lore("<dark_gray>多人遊戲 • マルチプレイヤー").click(
                event -> MultiplayerMenu.open(event.getPlayer())),
                PlusOption.MULTIPLAYER::check);

        // practice settings only if player's generator is of this instance
        MainMenu.INSTANCE.registerMainItem(1, 3,
                user -> new Item(Material.COMPARATOR, "<#E74FA1><bold>Practice Settings").click(
                        event -> {
                            ParkourPlayer pp = ParkourPlayer.getPlayer(event.getPlayer());
                            if (pp != null && pp.getGenerator() instanceof PracticeGenerator generator) {
                                generator.open();
                            }
                        }),
                player -> {
                    ParkourPlayer pp = ParkourPlayer.getPlayer(player);
                    return pp != null && pp.getGenerator() instanceof PracticeGenerator;
                });

        LobbyMenu.INSTANCE.registerMainItem(2, 2,
                user -> new Item(Material.ELYTRA, "<#2B97E2><bold>Invite").click(
                        event -> InviteMenu.open(event.getPlayer())),
                player -> {
                    ParkourUser user = ParkourUser.getUser(player);

                    // only show is user is parkourplayer and first player in session (the owner)
                    return user instanceof ParkourPlayer && user.getSession() instanceof MultiSession
                            && user.getSession().getPlayers().get(0) == user;
                });

        LobbyMenu.INSTANCE.registerMainItem(2, 1,
                user -> new Item(Material.CHEST, "<#EA9926><bold>View current lobbies")
                    .lore("<dark_gray>Aktuelle Lobbys ansehen • 查看当前大厅" ,
                            "<dark_gray>• 查看當前大廳 • Voir les lobbys actuels",
                            "<dark_gray>• 現在のロビーを表示 • Actuele lobby's bekijken")
                    .click(event -> ActiveMenu.open(event.getPlayer(), ActiveMenu.MenuSort.LEAST_OPEN_FIRST)),
                player -> true);

        logging().info("Loaded Infinite Parkour Plus in " + Time.timerEnd("enable") + "ms!");
    }

    @Override
    public void disable() {
        // save all gamemodes
        PlusGamemodes.TIME_TRIAL.getLeaderboard().write(false);
        PlusGamemodes.SPEED_JUMP.getLeaderboard().write(false);
        PlusGamemodes.HOURGLASS.getLeaderboard().write(false);
        PlusGamemodes.SPEED.getLeaderboard().write(false);

        PlusGamemodes.DUELS.getLeaderboard().write(false);
        PlusGamemodes.TEAM_SURVIVAL.getLeaderboard().write(false);
    }

    @Override
    public @Nullable GitElevator getElevator() {
        return null;
    }

    private void registerGamemode(Gamemode gamemode) {
        if (configuration.getFile("config").getBoolean("gamemodes." + gamemode.getName().toLowerCase() + ".enabled")) {
            IP.getRegistry().register(gamemode);
        }
    }

    /**
     * Returns the {@link Logging} belonging to this plugin.
     *
     * @return this plugin's {@link Logging} instance.
     */
    public static Logging logging() {
        return getPlugin().logging;
    }

    /**
     * Returns this plugin instance.
     *
     * @return the plugin instance.
     */
    public static IPP getPlugin() {
        return instance;
    }

    public static PlusConfiguration getConfiguration() {
        return configuration;
    }
}