package com.janboerman.invsee.fakes;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.Tag;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class FakeServer implements Server {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public String getName() {
        return "FakeServer";
    }

    @Override
    public String getVersion() {
        return "1.19.3";
    }

    @Override
    public String getBukkitVersion() {
        return "1.19.3-R0.1-SNAPSHOT";
    }

    @Override
    public String getMinecraftVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull File getPluginsFolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxPlayers(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getViewDistance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSimulationDistance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getWorldType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getGenerateStructures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxWorldSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAllowEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAllowNether() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasWhitelist() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWhitelist(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reloadWhitelist() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int broadcastMessage(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUpdateFolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getUpdateFolderFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getConnectionThrottle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerWaterSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksPerSpawns(@NotNull SpawnCategory spawnCategory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player getPlayer(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player getPlayerExact(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Player> matchPlayer(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player getPlayer(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID getPlayerUniqueId(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PluginManager getPluginManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BukkitScheduler getScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServicesManager getServicesManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<World> getWorlds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTickingWorlds() {
        return false;
    }

    @Override
    public World createWorld(WorldCreator worldCreator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unloadWorld(String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unloadWorld(World world, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable World getWorld(@NotNull NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WorldBorder createWorldBorder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapView getMap(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapView createMap(World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int i, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reloadData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public PluginCommand getPluginCommand(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dispatchCommand(CommandSender commandSender, String s) throws CommandException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Recipe getRecipe(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Recipe getCraftingRecipe(@NotNull ItemStack[] itemStacks, @NotNull World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ItemStack craftItem(@NotNull ItemStack[] itemStacks, @NotNull World world, @NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearRecipes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetRecipes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeRecipe(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSpawnRadius() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpawnRadius(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean shouldSendChatPreviews() {
        return false;
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return false;
    }

    @Override
    public boolean getHideOnlinePlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getOnlineMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAllowFlight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHardcore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int broadcast(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int broadcast(@NotNull Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int broadcast(@NotNull Component component, @NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OfflinePlayer getOfflinePlayerIfCached(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(@Nullable UUID uuid, @Nullable String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(@NotNull UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getIPBans() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void banIP(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unbanIP(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BanList getBanList(Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameMode getDefaultGameMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultGameMode(GameMode gameMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull CommandSender createCommandSender(@NotNull Consumer<? super Component> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getWorldContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Messenger getMessenger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HelpMap getHelpMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType, @NotNull Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i, @NotNull Component component) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int i, String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Merchant createMerchant(@Nullable Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Merchant createMerchant(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return -1; //negative implies that the value is not used :)
    }

    @Override
    public int getMonsterSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAnimalSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWaterUndergroundCreatureSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAmbientSpawnLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSpawnLimit(@NotNull SpawnCategory spawnCategory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimaryThread() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Component motd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMotd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Component shutdownMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getShutdownMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WarningState getWarningState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemFactory getItemFactory() {
        return FakeItemFactory.INSTANCE;
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Criteria getScoreboardCriteria(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CachedServerIcon getServerIcon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CachedServerIcon loadServerIcon(File file) throws IllegalArgumentException, Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public CachedServerIcon loadServerIcon(BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIdleTimeout(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIdleTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkData createChunkData(World world) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public ChunkData createVanillaChunkData(World world, int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BossBar createBossBar(String s, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey namespacedKey, String s, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeBossBar(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getEntity(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getTPS() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] getTickTimes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getAverageTickTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandMap getCommandMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Advancement getAdvancement(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Advancement> advancementIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockData createBlockData(Material material) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockData createBlockData(Material material, Consumer<BlockData> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockData createBlockData(String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockData createBlockData(Material material, String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Keyed> Tag<T> getTag(String s, NamespacedKey namespacedKey, Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Keyed> Iterable<Tag<T>> getTags(String s, Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LootTable getLootTable(NamespacedKey namespacedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Entity> selectEntities(CommandSender commandSender, String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull StructureManager getStructureManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnsafeValues getUnsafe() {
        return FakeUnsafeValues.INSTANCE;
    }

    @Override
    public Spigot spigot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reloadPermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reloadCommandAliases() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPermissionMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Component permissionMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerProfile createProfile(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerProfile createProfile(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerProfile createProfile(UUID uuid, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PlayerProfile createProfileExact(@Nullable UUID uuid, @Nullable String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCurrentTick() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStopping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MobGoals getMobGoals() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DatapackManager getDatapackManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PotionBrewer getPotionBrewer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RegionScheduler getRegionScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull AsyncScheduler getAsyncScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull GlobalRegionScheduler getGlobalRegionScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull Position position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull Position position, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int i, int i1, int i2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGlobalTickThread() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWhitelistEnforced(boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWhitelistEnforced() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isResourcePackRequired() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResourcePack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResourcePackHash() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResourcePackPrompt() {
        throw new UnsupportedOperationException();
    }

}
