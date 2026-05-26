package me.davidml16.baul;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import me.davidml16.baul.animations.Animation;
import me.davidml16.baul.animations.AnimationHandler;
import me.davidml16.baul.api.CubeletsAPI;
import me.davidml16.baul.api.PointsAPI;
import me.davidml16.baul.database.DatabaseHandler;
import me.davidml16.baul.events.*;
import me.davidml16.baul.handlers.*;
import me.davidml16.baul.holograms.HologramHandler;
import me.davidml16.baul.holograms.HologramImplementation;
import me.davidml16.baul.sync.SyncManager;
import me.davidml16.baul.tasks.*;
import me.davidml16.baul.utils.ConfigUpdater;
import me.davidml16.baul.utils.FireworkUtil;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Main extends JavaPlugin {

    public static ConsoleCommandSender log;
    private static Main main;
    @Getter
    private final List<String> templates = Collections.singletonList("example");
    @Getter
    private MetricsLite metrics;
    private CubeletsAPI cubeletsAPI;
    private PointsAPI pointsAPI;
    @Getter
    private ProtocolManager protocolManager;
    @Getter
    private HologramTask hologramTask;
    private DataSaveTask dataSaveTask;
    @Getter
    private LiveGuiTask liveGuiTask;
    private DataCacheTask dataCacheTask;
    @Getter
    private MachineEffectsTask machineEffectsTask;
    @Getter
    private LanguageHandler languageHandler;
    @Getter
    private DatabaseHandler databaseHandler;
    @Getter
    private PlayerDataHandler playerDataHandler;
    @Getter
    private CubeletTypesHandler cubeletTypesHandler;
    @Getter
    private CubeletRarityHandler cubeletRarityHandler;
    @Getter
    private CubeletRewardHandler cubeletRewardHandler;
    @Getter
    private CubeletMachineHandler cubeletMachineHandler;
    @Getter
    private HologramHandler hologramHandler;
    @Getter
    private CubeletOpenHandler cubeletOpenHandler;
    @Getter
    private AnimationHandler animationHandler;
    @Getter
    private CubeletCraftingHandler cubeletCraftingHandler;
    @Getter
    private EconomyHandler economyHandler;
    @Getter
    private LayoutHandler layoutHandler;
    @Getter
    private ConversationHandler conversationHandler;
    @Getter
    private TransactionHandler transactionHandler;
    @Getter
    private MenuHandler menuHandler;
    @Getter
    private FireworkUtil fireworkUtil;
    @Getter
    private PluginHandler pluginHandler;
    @Getter
    @Setter
    private int playerCount;
    @Getter
    private SyncManager syncManager;
    private Map<String, Object> settings;
    private CommandMap commandMap;

    public static Main get() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;
        log = Bukkit.getConsoleSender();
        metrics = new MetricsLite(this, 7349);

        settings = new HashMap<>();

        migrateOldConfig();

        saveDefaultConfig();
        try {
            ConfigUpdater.update(this, "config.yml", new File(main.getDataFolder(), "config.yml"), Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();

        if (!XMaterial.supports(1, 16)) {
            getLogger().severe("***   Plugin only supports 1.16+ versions.");
            getLogger().severe("***   If you are using 1.8+ versions, please use the latest plugin version 2.1.8.");
            getLogger().severe("***   Or if you are using 1.13+ versions, please use the latest plugin version 2.4.7.");
            setEnabled(false);
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("*** ProtocolLib is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            setEnabled(false);
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        registerSettings();

        pluginHandler = new PluginHandler(this);

        transactionHandler = new TransactionHandler(this);

        languageHandler = new LanguageHandler(this, getConfig().getString("Language").toLowerCase());
        languageHandler.pushMessages();

        databaseHandler = new DatabaseHandler(this);
        databaseHandler.openConnection();
        databaseHandler.loadTables();

        animationHandler = new AnimationHandler(this);
        animationHandler.loadAnimations();

        cubeletMachineHandler = new CubeletMachineHandler(this);
        cubeletMachineHandler.loadMachines();
        cubeletMachineHandler.setClickType(getConfig().getString("CubeletMachine.ClickType"));

        cubeletTypesHandler = new CubeletTypesHandler(this);
        cubeletTypesHandler.loadTypes();

        cubeletRarityHandler = new CubeletRarityHandler(this);
        cubeletRarityHandler.loadRarities();

        cubeletRewardHandler = new CubeletRewardHandler(this);
        cubeletRewardHandler.loadRewards();

        cubeletTypesHandler.printLog();

        economyHandler = new EconomyHandler();
        economyHandler.load();

        cubeletCraftingHandler = new CubeletCraftingHandler(this);
        cubeletCraftingHandler.loadCrafting();

        playerDataHandler = new PlayerDataHandler(this);

        hologramHandler = new HologramHandler(this);

        if (hologramHandler.getImplementation() == null) {
            getLogger().severe("*** HolographicDisplays or Decent Holograms is not installed or not enabled. ***");
            getLogger().severe("*** Now the plugin will be disabled. ***");
            setEnabled(false);
            return;
        }

        int distance = getConfig().getInt("Holograms.VisibilityDistance");
        hologramHandler.setVisibilityDistance(distance * distance);

        hologramHandler.getColorAnimation().setColors(getConfig().getStringList("Holograms.ColorAnimation"));
        hologramHandler.getImplementation().loadHolograms();

        playerDataHandler.loadAllPlayerData();

        hologramTask = new HologramTask(this);
        hologramTask.start();

        dataSaveTask = new DataSaveTask(this);
        dataSaveTask.start();

        dataCacheTask = new DataCacheTask(this);
        dataCacheTask.start();

        machineEffectsTask = new MachineEffectsTask(this);
        machineEffectsTask.start();

        cubeletOpenHandler = new CubeletOpenHandler(this);

        liveGuiTask = new LiveGuiTask(this);
        if (isSetting("LiveGuiUpdates")) liveGuiTask.start();

        layoutHandler = new LayoutHandler(this);

        menuHandler = new MenuHandler(this);

        menuHandler.setClickType(getConfig().getString("Rewards.Preview.ClickType"));

        conversationHandler = new ConversationHandler(this);

        fireworkUtil = new FireworkUtil(this);

        cubeletsAPI = new CubeletsAPI(this);
        pointsAPI = new PointsAPI(this);

        registerCommands();
        registerEvents();

        syncManager = new SyncManager(this);

        playerCount = getServer().getOnlinePlayers().size();

        String authors = String.join(", ", getDescription().getAuthors());

        PluginDescriptionFile pdf = getDescription();
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eBaul Enabled!"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aVersion: &b" + pdf.getVersion()));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aAuthor: &b" + authors));
        log.sendMessage("");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            settings.put("placeholderapi", true);
        } else {
            settings.put("placeholderapi", false);
        }

    }

    @Override
    public void onDisable() {

        PluginDescriptionFile pdf = getDescription();
        String authors = String.join(", ", pdf.getAuthors());
        log.sendMessage("");
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eBaul Disabled!"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aVersion: &b" + pdf.getVersion()));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aAuthor: &b" + authors));
        log.sendMessage("");

        if (hologramHandler != null && hologramHandler.getImplementation() != null)
            hologramHandler.getImplementation().removeHolograms();

        main.getPlayerDataHandler().saveAllPlayerDataSync();

        for (Animation task : new ArrayList<>(main.getAnimationHandler().getTasks())) {
            task.stop();
        }
        main.getAnimationHandler().getTasks().clear();

        for (Entity entity : main.getAnimationHandler().getEntities()) {
            entity.remove();
        }
        main.getAnimationHandler().getEntities().clear();

        if (hologramTask != null) hologramTask.stop();
        if (dataSaveTask != null) dataSaveTask.stop();
        if (machineEffectsTask != null) machineEffectsTask.stop();
        if (syncManager != null) syncManager.shutdown();
        if (databaseHandler != null) databaseHandler.getDatabaseConnection().stop();
    }

    public void registerSettings() {
        settings.put("Crafting", getConfig().getBoolean("Crafting"));

        settings.put("Rewards.Broadcast", getConfig().getBoolean("Rewards.Broadcast"));

        settings.put("LoginReminder", getConfig().getBoolean("LoginReminder"));

        settings.put("CubeletsCommand", getConfig().getBoolean("NoCubelets.ExecuteCommand"));
        settings.put("NoCubelets.ExecuteCommand", getConfig().getBoolean("NoCubelets.ExecuteCommand"));
        settings.put("NoCubelets.Command", getConfig().getString("NoCubelets.Command"));
        settings.put("NoCubelets.Executor", getConfig().getString("NoCubelets.Executor"));

        settings.put("Rewards.Duplication.Enabled", getConfig().getBoolean("Rewards.Duplication.Enabled"));
        settings.put("Rewards.Duplication.PointsCommand", getConfig().getString("Rewards.Duplication.PointsCommand"));
        settings.put("Rewards.PermissionCommand", getConfig().getString("Rewards.PermissionCommand"));

        settings.put("NoGuiMode", getConfig().getBoolean("NoGuiMode"));

        settings.put("AnimationsByPlayer", getConfig().getBoolean("AnimationsByPlayer"));

        settings.put("SerializeBase64", getConfig().getBoolean("SerializeBase64"));
        settings.put("Rewards.AutoSorting", getConfig().getBoolean("Rewards.AutoSorting"));
        settings.put("UseKeys", getConfig().getBoolean("UseKeys"));

        settings.put("HDVisibleToAllPlayers", getConfig().getBoolean("Holograms.Duplication.VisibleToAllPlayers"));

        settings.put("LiveGuiUpdates", getConfig().getBoolean("LiveGuiUpdates"));

        settings.put("Rewards.Preview.Enabled", getConfig().getBoolean("Rewards.Preview.Enabled"));

        settings.put("GiftCubeletsCommand", getConfig().getBoolean("GiftCubeletsCommand"));

        settings.put("GiftMenuSpareHeadType", getConfig().getString("GiftMenuSpareHeadType"));
        settings.put("GiftMenuSpareHeadValue", getConfig().getString("GiftMenuSpareHeadValue"));
    }

    public boolean isSetting(String key) {
        return settings.containsKey(key) && (boolean) settings.get(key);
    }

    public String getSetting(String key) {
        return settings.containsKey(key) ? (String) settings.get(key) : "";
    }

    public DatabaseHandler getDatabase() {
        return databaseHandler;
    }

    public CubeletMachineHandler getCubeletBoxHandler() {
        return cubeletMachineHandler;
    }

    public HologramImplementation getHologramImplementation() {
        return hologramHandler.getImplementation();
    }

    public boolean playerHasPermission(Player p, String permission) {
        return p.hasPermission(permission) || p.isOp();
    }

    private void migrateOldConfig() {
        File oldFolder = new File("plugins/ACubelets");
        File newFolder = getDataFolder();
        if (!oldFolder.exists() || newFolder.exists()) return;

        log.sendMessage(me.davidml16.baul.utils.Colorize.format(""));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <gold>Detectada instalación previa de ACubelets.</gold>"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <gold>Migrando archivos a plugins/Baul/...</gold>"));

        File[] files = oldFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    copyDirectory(file, new File(newFolder, file.getName()));
                } else {
                    java.nio.file.Files.copy(file.toPath(), new File(newFolder, file.getName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <red>Error migrando " + file.getName() + ": " + e.getMessage() + "</red>"));
            }
        }

        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <green>Migración completada!</green>"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format(""));
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) target.mkdirs();
        File[] files = source.listFiles();
        if (files == null) return;
        for (File file : files) {
            File dest = new File(target, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, dest);
            } else {
                java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void registerCommands() {
        Field bukkitCommandMap;
        try {
            bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register("baul", new me.davidml16.baul.commands.baul.CoreCommand(getConfig().getString("Commands.Main")
                    .toLowerCase()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new Event_Interact(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_JoinQuit(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_Damage(), this);
        Bukkit.getPluginManager().registerEvents(new Event_Menus(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_Block(this), this);
    }

}
