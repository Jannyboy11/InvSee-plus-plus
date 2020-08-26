package com.janboerman.invsee.spigot.perworldinventory;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;

public class FakePlayer implements Player {

    private final Server server;
    private final UUID uuid;
    private final String name;
    private String displayName;
    private String playerListName;
    private String playerListHeader;
    private String playerListFooter;
    private Location compassTarget;
    private Location location;
    private int fireTicks;
    private boolean persistent;
    private Entity passenger;
    private Entity vehicle;
    private float fallDistance;
    private EntityDamageEvent lastDamageCause;
    private int ticksLived;
    private boolean customNameVisible;
    private boolean glowing;
    private boolean invulnerable;
    private boolean silent;
    private int portalCooldown;
    private boolean sneaking;
    private boolean sprinting;
    private boolean sleepingIgnored;
    private EnumMap<Statistic, Integer> statistics = new EnumMap<>(Statistic.class);
    private Location bedSpawnLocation;
    private long playerTime;
    private WeatherType weatherType;
    private int level;
    private float exp;
    private Integer totalExperience;
    private float exhaustion;
    private float saturation;
    private int foodLevel;
    private boolean allowFlight;
    private boolean flying;
    private float flySpeed;
    private float walkSpeed;
    private Scoreboard scoreboard;
    private boolean healthScaled;
    private double healthScale;
    private Entity spectatorTarget;
    private int clientViewDistance;
    private MainHand mainHand = MainHand.RIGHT;
    private ItemStack itemOnCursor;
    private EnumMap<Material, Integer> cooldowns = new EnumMap<>(Material.class);
    private GameMode gameMode;
    private Set<NamespacedKey> discoveredRecipes = new HashSet<>();
    private Entity leftShoulder, rightShoulder;
    private Integer remainingAir, maximumAir;
    private int maximumNoDamageTicks;
    private Double lastDamage;
    private int noDamageTicks;
    private Map<PotionEffectType, PotionEffect> potionEffects = new HashMap<>();
    private boolean removeWhenFarAway;
    private boolean canPickupItems;
    private Entity leashHolder;
    private boolean gliding;
    private boolean swimming;
    private boolean collidable;
    private Map<MemoryKey<?>, Object> memory = new HashMap<>();
    private double health;
    private double absorptionAmount;
    private String customName;
    private HashMap<String, Map<Plugin, MetadataValue>> metadata = new HashMap<>();
    private final PermissibleBase permissible;
    private boolean operator;
    private PersistentDataContainer persistentDataContainer;
    private final FakeInventory enderChest;
    private final FakePlayerInventory inventory;
    private final EnumMap<Attribute, AttributeInstance> attributes = new EnumMap<>(Attribute.class);
    private final long firstPlayed, lastPlayed;

    public FakePlayer(UUID uniqueId, String name, Server server) {
        this.uuid = uniqueId;
        this.name = name;
        this.server = server;
        setDisplayName(name);
        setPlayerListName(name);
        setGameMode(server.getDefaultGameMode());
        this.permissible = new PermissibleBase(this);
        this.enderChest = new FakeInventory(InventoryType.ENDER_CHEST, new ItemStack[27], this);
        this.inventory = new FakePlayerInventory(new ItemStack[9 * 4 + 4 + 1], this);
        this.clientViewDistance = server.getViewDistance();

        registerAttribute(Attribute.GENERIC_MAX_HEALTH);
        registerAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        registerAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        registerAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        registerAttribute(Attribute.GENERIC_ARMOR);
        registerAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        //not yet present in 1.15.2
        //registerAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
        registerAttribute(Attribute.GENERIC_ATTACK_SPEED);
        registerAttribute(Attribute.GENERIC_LUCK);

        this.firstPlayed = 0;
        this.lastPlayed = System.currentTimeMillis();
    }

    private AttributeInstance registerAttribute(Attribute attribute) {
        AttributeInstance instance = new FakeAttributeInstance(attribute);
        attributes.put(attribute, instance);
        return instance;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void setDisplayName(@Nullable String s) {
        this.displayName = s;
    }

    @NotNull
    @Override
    public String getPlayerListName() {
        return playerListName;
    }

    @Override
    public void setPlayerListName(@Nullable String s) {
        this.playerListName = s;
    }

    @Nullable
    @Override
    public String getPlayerListHeader() {
        return playerListHeader;
    }

    @Nullable
    @Override
    public String getPlayerListFooter() {
        return playerListFooter;
    }

    @Override
    public void setPlayerListHeader(@Nullable String s) {
        this.playerListHeader = s;
    }

    @Override
    public void setPlayerListFooter(@Nullable String s) {
        this.playerListFooter = s;
    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String s, @Nullable String s1) {
        setPlayerListHeader(s);
        setPlayerListFooter(s1);
    }

    @Override
    public void setCompassTarget(@NotNull Location location) {
        this.compassTarget = location;
    }

    @NotNull
    @Override
    public Location getCompassTarget() {
        if (compassTarget == null) {
            return getBedSpawnLocation();
        } else {
            return compassTarget;
        }
    }

    @Nullable
    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(@NotNull String s) {
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
    }

    @Override
    public void sendRawMessage(@NotNull String s) {
    }

    @Override
    public void kickPlayer(@Nullable String s) {
    }

    @Override
    public void chat(@NotNull String s) {
    }

    @Override
    public boolean performCommand(@NotNull String s) {
        return false;
    }

    @NotNull
    @Override
    public Location getLocation() {
        if (location == null) {
            return getBedLocation();
        } else {
            return location;
        }
    }

    @Nullable
    @Override
    public Location getLocation(@Nullable Location location) {
        if (location == null || this.location == null) return null;
        location.setX(this.location.getX());
        location.setY(this.location.getY());
        location.setZ(this.location.getZ());
        location.setYaw(this.location.getYaw());
        location.setPitch(this.location.getPitch());
        location.setWorld(this.location.getWorld());
        return location;
    }

    @Override
    public void setVelocity(@NotNull Vector vector) {
    }

    @NotNull
    @Override
    public Vector getVelocity() {
        return new Vector(0, 0, 0);
    }

    @Override
    public double getHeight() {
        if (isSleeping()) {
            return 0.2;
        }
        if (isSwimming()) {
            return 0.6;
        }
        if (isSneaking()) {
            return 1.5;
        }
        return 1.8;
    }

    @Override
    public double getWidth() {
        if (isSleeping()) return 0.2;
        return 0.6;
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        double halfWidth = getWidth() / 2;
        Location loc = getLocation();
        double minX = loc.getX() - halfWidth;
        double maxX = loc.getX() + halfWidth;
        double minZ = loc.getZ() - halfWidth;
        double maxZ = loc.getZ() + halfWidth;
        double minY = loc.getY();
        double maxY = loc.getY() + getHeight();
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @NotNull
    @Override
    public World getWorld() {
        return getLocation().getWorld();
    }

    @Override
    public void setRotation(float v, float v1) {
        if (this.location == null) this.location = getLocation().clone();
        this.location.setYaw(v);
        this.location.setPitch(v1);
    }

    @Override
    public boolean teleport(@NotNull Location location) {
        this.location = location.clone();
        return true;
    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause teleportCause) {
        return teleport(location);
    }

    @Override
    public boolean teleport(@NotNull Entity entity) {
        return teleport(entity.getLocation());
    }

    @Override
    public boolean teleport(@NotNull Entity entity, @NotNull PlayerTeleportEvent.TeleportCause teleportCause) {
        return teleport(entity.getLocation(), teleportCause);
    }

    @NotNull
    @Override
    public List<Entity> getNearbyEntities(double v, double v1, double v2) {
        return List.copyOf(getWorld().getNearbyEntities(getLocation(), v, v1, v2));
    }

    @Override
    public int getEntityId() {
        return -1;
    }

    @Override
    public int getFireTicks() {
        return fireTicks;
    }

    @Override
    public int getMaxFireTicks() {
        return 16;
    }

    @Override
    public void setFireTicks(int i) {
        this.fireTicks = i;
    }

    @Override
    public void remove() {
    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void sendMessage(@NotNull String s) {
    }

    @Override
    public void sendMessage(@NotNull String[] strings) {
    }

    @NotNull
    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public void setPersistent(boolean b) {
        this.persistent = b;
    }

    @Nullable
    @Override
    public Entity getPassenger() {
        return passenger;
    }

    @Override
    public boolean setPassenger(@NotNull Entity entity) {
        if (entity != this) {
            this.passenger = entity;
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public List<Entity> getPassengers() {
        List<Entity> passengers = new ArrayList<>(0);
        Entity passenger = getPassenger();
        if (passenger != null) {
            passengers.add(passenger);
            passengers.addAll(passenger.getPassengers());
        }
        return passengers;
    }

    @Override
    public boolean addPassenger(@NotNull Entity entity) {
        Entity passenger = getPassenger();
        if (passenger == null) {
            return setPassenger(entity);
        } else {
            return passenger.addPassenger(entity);
        }
    }

    @Override
    public boolean removePassenger(@NotNull Entity entity) {
        Entity passenger = getPassenger();
        if (passenger == null) return false;
        if (Objects.equals(passenger, entity)) {
            eject();
            return true;
        } else {
            return passenger.removePassenger(entity);
        }
    }

    @Override
    public boolean isEmpty() {
        return getPassenger() == null;
    }

    @Override
    public boolean eject() {
        boolean ejected = passenger != null;
        this.passenger = null;
        return ejected;
    }

    @Override
    public float getFallDistance() {
        return fallDistance;
    }

    @Override
    public void setFallDistance(float v) {
        this.fallDistance = v;
    }

    @Override
    public void setLastDamageCause(@Nullable EntityDamageEvent entityDamageEvent) {
        this.lastDamageCause = entityDamageEvent;
    }

    @Nullable
    @Override
    public EntityDamageEvent getLastDamageCause() {
        return lastDamageCause;
    }

    @NotNull
    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getTicksLived() {
        return ticksLived;
    }

    @Override
    public void setTicksLived(int i) {
        this.ticksLived = i;
    }

    @Override
    public void playEffect(@NotNull EntityEffect entityEffect) {
    }

    @NotNull
    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public boolean isInsideVehicle() {
        return getVehicle() != null;
    }

    @Override
    public boolean leaveVehicle() {
        boolean result = isInsideVehicle();
        vehicle = null;
        return result;
    }

    @Nullable
    @Override
    public Entity getVehicle() {
        return vehicle;
    }

    @Override
    public void setCustomNameVisible(boolean b) {
        this.customNameVisible = b;
    }

    @Override
    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    @Override
    public void setGlowing(boolean b) {
        this.glowing = b;
    }

    @Override
    public boolean isGlowing() {
        return glowing;
    }

    @Override
    public void setInvulnerable(boolean b) {
        this.invulnerable = b;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setSilent(boolean b) {
        this.silent = b;
    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean b) {
    }

    @Override
    public int getPortalCooldown() {
        return portalCooldown;
    }

    @Override
    public void setPortalCooldown(int i) {
        portalCooldown = i;
    }

    @NotNull
    @Override
    public Set<String> getScoreboardTags() {
        return Set.of();
    }

    @Override
    public boolean addScoreboardTag(@NotNull String s) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String s) {
        return false;
    }

    @NotNull
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.MOVE;
    }

    @NotNull
    @Override
    public BlockFace getFacing() {
        return BlockFace.DOWN;
    }

    @NotNull
    @Override
    public Pose getPose() {
        return Pose.STANDING;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public void setSneaking(boolean b) {
        this.sneaking = b;
    }

    @Override
    public boolean isSprinting() {
        return sprinting;
    }

    @Override
    public void setSprinting(boolean b) {
        this.sprinting = b;
        if (b) {
            getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
        } else {
            getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        }
    }

    @Override
    public void saveData() {
        //do the PlayerProfile lookup thing?? I think that is not necessary though.
    }

    @Override
    public void loadData() {
        //do the PlayerProfile lookup thing?? I think that is not necessary though.
    }

    @Override
    public void setSleepingIgnored(boolean b) {
        sleepingIgnored = b;
    }

    @Override
    public boolean isSleepingIgnored() {
        return sleepingIgnored;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return getServer().getWhitelistedPlayers().contains(this);
    }

    @Override
    public void setWhitelisted(boolean b) {
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return getServer().getPlayer(getUniqueId());
    }

    @Override
    public long getFirstPlayed() {
        return firstPlayed;
    }

    @Override
    public long getLastPlayed() {
        return lastPlayed;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Nullable
    @Override
    public Location getBedSpawnLocation() {
        if (bedSpawnLocation == null) {
            return getServer().getWorlds().get(0).getSpawnLocation();
        } else {
            return bedSpawnLocation.clone();
        }
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        statistics.compute(statistic, (stat, existing) -> existing == null ? 1 : existing + 1);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        statistics.compute(statistic, (stat, existing) -> existing == null ? -1 : existing - 1);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        statistics.compute(statistic, (stat, existing) -> existing == null ? i : existing + i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        statistics.compute(statistic, (stat, existing) -> existing == null ? -i : existing - i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        statistics.put(statistic, i);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        return statistics.getOrDefault(statistic, 0);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        incrementStatistic(statistic);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        decrementStatistic(statistic);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return getStatistic(statistic);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        incrementStatistic(statistic, i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        decrementStatistic(statistic, i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        setStatistic(statistic, i);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        incrementStatistic(statistic);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        decrementStatistic(statistic);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        return getStatistic(statistic);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) throws IllegalArgumentException {
        incrementStatistic(statistic, i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
        decrementStatistic(statistic, i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
        setStatistic(statistic, i);
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location) {
        this.bedSpawnLocation = location == null ? null : location.clone();
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location, boolean b) {
        setBedSpawnLocation(location);
    }

    @Override
    public void playNote(@NotNull Location location, byte b, byte b1) {
    }

    @Override
    public void playNote(@NotNull Location location, @NotNull Instrument instrument, @NotNull Note note) {
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, float v, float v1) {
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String s, float v, float v1) {
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory soundCategory, float v, float v1) {
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1) {
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
    }

    @Override
    public void stopSound(@NotNull String s) {
    }

    @Override
    public void stopSound(@NotNull Sound sound, @Nullable SoundCategory soundCategory) {
    }

    @Override
    public void stopSound(@NotNull String s, @Nullable SoundCategory soundCategory) {
    }

    @Override
    public void playEffect(@NotNull Location location, @NotNull Effect effect, int i) {
        getWorld().playEffect(location, effect, i);
    }

    @Override
    public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T t) {
        getWorld().playEffect(location, effect, t);
    }

    @Override
    public void sendBlockChange(@NotNull Location location, @NotNull Material material, byte b) {
    }

    @Override
    public void sendBlockChange(@NotNull Location location, @NotNull BlockData blockData) {
    }

    @Override
    public boolean sendChunkChange(@NotNull Location location, int i, int i1, int i2, @NotNull byte[] bytes) {
        return false;
    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings) throws IllegalArgumentException {
    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings, @NotNull DyeColor dyeColor) throws IllegalArgumentException {
    }

    @Override
    public void sendMap(@NotNull MapView mapView) {
    }

    @Override
    public void updateInventory() {
    }

    @Override
    public void setPlayerTime(long l, boolean b) {
        this.playerTime = l;
    }

    @Override
    public long getPlayerTime() {
        return playerTime;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {
        this.playerTime = 0;
    }

    @Override
    public void setPlayerWeather(@NotNull WeatherType weatherType) {
        this.weatherType = weatherType;
    }

    @Nullable
    @Override
    public WeatherType getPlayerWeather() {
        return weatherType;
    }

    @Override
    public void resetPlayerWeather() {
        setPlayerWeather(WeatherType.CLEAR);
    }

    @Override
    public void giveExp(int i) {

    }

    @Override
    public void giveExpLevels(int i) {

    }

    @Override
    public float getExp() {
        return exp;
    }

    @Override
    public void setExp(float v) {
        this.exp = v;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int i) {
        this.level = i;
    }

    @Override
    public int getTotalExperience() {
        if (totalExperience != null) return totalExperience;
        int level = getLevel();
        if (0 <= level && level <= 16) {
            return (6 + level) * level;
        } else if (17 <= level && level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level - 162.5 * level + 2220);
        }
    }

    @Override
    public void setTotalExperience(int i) {
        this.totalExperience = i;
    }

    @Override
    public void sendExperienceChange(float v) {
    }

    @Override
    public void sendExperienceChange(float v, int i) {
    }

    @Override
    public float getExhaustion() {
        return exhaustion;
    }

    @Override
    public void setExhaustion(float v) {
        this.exhaustion = v;
    }

    @Override
    public float getSaturation() {
        return saturation;
    }

    @Override
    public void setSaturation(float v) {
        this.saturation = v;
    }

    @Override
    public int getFoodLevel() {
        return foodLevel;
    }

    @Override
    public void setFoodLevel(int i) {
        this.foodLevel = i;
    }

    @Override
    public boolean getAllowFlight() {
        return allowFlight;
    }

    @Override
    public void setAllowFlight(boolean b) {
        this.allowFlight = b;
    }

    @Override
    public void hidePlayer(@NotNull Player player) {
    }

    @Override
    public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {
    }

    @Override
    public void showPlayer(@NotNull Player player) {
    }

    @Override
    public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {
    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean isFlying() {
        return flying;
    }

    @Override
    public void setFlying(boolean b) {
        this.flying = b;
    }

    @Override
    public void setFlySpeed(float v) throws IllegalArgumentException {
        this.flySpeed = v;
    }

    @Override
    public void setWalkSpeed(float v) throws IllegalArgumentException {
        this.walkSpeed = v;
    }

    @Override
    public float getFlySpeed() {
        return flySpeed;
    }

    @Override
    public float getWalkSpeed() {
        return walkSpeed;
    }

    @Override
    public void setTexturePack(@NotNull String s) {
    }

    @Override
    public void setResourcePack(@NotNull String s) {
    }

    @Override
    public void setResourcePack(@NotNull String s, @NotNull byte[] bytes) {
    }

    @NotNull
    @Override
    public Scoreboard getScoreboard() {
        if (scoreboard == null) {
            return getServer().getScoreboardManager().getMainScoreboard();
        } else {
            return scoreboard;
        }
    }

    @Override
    public void setScoreboard(@NotNull Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        this.scoreboard = scoreboard;
    }

    @Override
    public boolean isHealthScaled() {
        return healthScaled;
    }

    @Override
    public void setHealthScaled(boolean b) {
        this.healthScaled = b;
    }

    @Override
    public void setHealthScale(double v) throws IllegalArgumentException {
        this.healthScale = v;
    }

    @Override
    public double getHealthScale() {
        return healthScale;
    }

    @Nullable
    @Override
    public Entity getSpectatorTarget() {
        return spectatorTarget;
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {
        this.spectatorTarget = entity;
    }

    @Override
    public void sendTitle(@Nullable String s, @Nullable String s1) {
    }

    @Override
    public void sendTitle(@Nullable String s, @Nullable String s1, int i, int i1, int i2) {
    }

    @Override
    public void resetTitle() {
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i) {
        getWorld().spawnParticle(particle, location, i);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i) {
        getWorld().spawnParticle(particle, v, v1, v2, i);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, @Nullable T t) {
        getWorld().spawnParticle(particle, location, i, t);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, @Nullable T t) {
        getWorld().spawnParticle(particle, v, v1, v2, i, t);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2) {
        getWorld().spawnParticle(particle, location, i, v, v1, v2);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5) {
        getWorld().spawnParticle(particle, v, v1, v2, i, v3, v4, v5);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, @Nullable T t) {
        getWorld().spawnParticle(particle, location, i, v, v1, v2, t);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, @Nullable T t) {
        getWorld().spawnParticle(particle, v, v1, v2, i, v3, v4, v5, t);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, double v3) {
        getWorld().spawnParticle(particle, location, i, v, v1, v2, v3);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6) {
        getWorld().spawnParticle(particle, v, v1, v2, i, v3, v4, v5, v6);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, double v3, @Nullable T t) {
        getWorld().spawnParticle(particle, location, i, v, v1, v2, v3, t);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, @Nullable T t) {
        getWorld().spawnParticle(particle, v, v1, v2, i, v3, v4, v5, v6, t);
    }

    @NotNull
    @Override
    public AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return clientViewDistance;
    }

    @NotNull
    @Override
    public String getLocale() {
        return Locale.ROOT.toString();
    }

    @Override
    public void updateCommands() {
    }

    @Override
    public void openBook(@NotNull ItemStack itemStack) {
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return new Player.Spigot();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return Map.of(); //can't be bothered to implement this properly. ¯\_(ツ)_/¯
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @NotNull
    @Override
    public Inventory getEnderChest() {
        return enderChest;
    }

    @NotNull
    @Override
    public MainHand getMainHand() {
        return this.mainHand;
    }

    @Override
    public boolean setWindowProperty(@NotNull InventoryView.Property property, int i) {
        return false;
    }

    @NotNull
    @Override
    public InventoryView getOpenInventory() {
        return new InventoryView() {
            @NotNull
            @Override
            public Inventory getTopInventory() {
                return FakePlayer.this.getInventory(); //crafting slots?!
            }

            @NotNull
            @Override
            public Inventory getBottomInventory() {
                return FakePlayer.this.getInventory();
            }

            @NotNull
            @Override
            public HumanEntity getPlayer() {
                return FakePlayer.this;
            }

            @NotNull
            @Override
            public InventoryType getType() {
                return InventoryType.PLAYER;
            }

            @NotNull
            @Override
            public String getTitle() {
                return InventoryType.PLAYER.getDefaultTitle();
            }
        };
    }

    @Nullable
    @Override
    public InventoryView openInventory(@NotNull Inventory inventory) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openWorkbench(@Nullable Location location, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openEnchanting(@Nullable Location location, boolean b) {
        return null;
    }

    @Override
    public void openInventory(@NotNull InventoryView inventoryView) {
        openInventory(inventoryView.getTopInventory());
    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Villager villager, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Merchant merchant, boolean b) {
        return null;
    }

    @Override
    public void closeInventory() {

    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInHand(@Nullable ItemStack itemStack) {
        getInventory().setItemInMainHand(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getItemOnCursor() {
        return itemOnCursor;
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack itemStack) {
        this.itemOnCursor = itemStack;
    }

    @Override
    public boolean hasCooldown(@NotNull Material material) {
        return getCooldown(material) > 0;
    }

    @Override
    public int getCooldown(@NotNull Material material) {
        return cooldowns.getOrDefault(material, 0);
    }

    @Override
    public void setCooldown(@NotNull Material material, int i) {
        cooldowns.put(material, i);
    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public boolean sleep(@NotNull Location location, boolean b) {
        return false;
    }

    @Override
    public void wakeup(boolean b) {
    }

    @NotNull
    @Override
    public Location getBedLocation() {
        return getBedSpawnLocation();
    }

    @NotNull
    @Override
    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public void setGameMode(@NotNull GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Override
    public int getExpToLevel() {
        int level = getLevel();
        if (0 <= level && level <= 15) {
            return 2 * level + 7;
        } else if (16 <= level && level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }

    @Override
    public boolean discoverRecipe(@NotNull NamespacedKey namespacedKey) {
        return getDiscoveredRecipes().add(namespacedKey);
    }

    @Override
    public int discoverRecipes(@NotNull Collection<NamespacedKey> collection) {
        int result = 0;
        for (var key : collection) {
            if (discoverRecipe(key)) {
                result += 1;
            }
        }
        return result;
    }

    @Override
    public boolean undiscoverRecipe(@NotNull NamespacedKey namespacedKey) {
        return getDiscoveredRecipes().remove(namespacedKey);
    }

    @Override
    public int undiscoverRecipes(@NotNull Collection<NamespacedKey> collection) {
        int result = 0;
        for (var key : collection) {
            if (undiscoverRecipe(key)) {
                result += 1;
            }
        }
        return result;
    }

    @Override
    public boolean hasDiscoveredRecipe(@NotNull NamespacedKey namespacedKey) {
        return getDiscoveredRecipes().contains(namespacedKey);
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return discoveredRecipes;
    }

    @Nullable
    @Override
    public Entity getShoulderEntityLeft() {
        return leftShoulder;
    }

    @Override
    public void setShoulderEntityLeft(@Nullable Entity entity) {
        leftShoulder = entity;
    }

    @Nullable
    @Override
    public Entity getShoulderEntityRight() {
        return rightShoulder;
    }

    @Override
    public void setShoulderEntityRight(@Nullable Entity entity) {
        rightShoulder = entity;
    }

    @Override
    public boolean dropItem(boolean b) {
        return false;
    }

    @Override
    public double getEyeHeight() {
        //not sure whether this is correct but whatever!
        if (isSneaking()) {
            return 1.3;
        } else if (isSwimming() || isGliding()) {
            return 0.1;
        }
        return 1.5;
    }

    @Override
    public double getEyeHeight(boolean b) {
        if (b) {
            return getEyeHeight();
        } else {
            return 1.5; //not sure!
        }
    }

    @NotNull
    @Override
    public Location getEyeLocation() {
        return getLocation().add(0, getEyeHeight(), 0);
    }

    @NotNull
    @Override
    public List<Block> getLineOfSight(@Nullable Set<Material> set, int i) {
        return List.of();
    }

    @NotNull
    @Override
    public Block getTargetBlock(@Nullable Set<Material> set, int i) {
        return getLocation().getBlock();
    }

    @NotNull
    @Override
    public List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> set, int i) {
        return List.of();
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int i) {
        return null;
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int i, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double v) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double v, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        if (remainingAir == null) return getMaximumAir();
        return remainingAir;
    }

    @Override
    public void setRemainingAir(int i) {
        this.remainingAir = i;
    }

    @Override
    public int getMaximumAir() {
        if (maximumAir == null) maximumAir = 20; //is this correct?
        return maximumAir;
    }

    @Override
    public void setMaximumAir(int i) {
        this.maximumAir = i;
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return maximumNoDamageTicks;
    }

    @Override
    public void setMaximumNoDamageTicks(int i) {
        maximumNoDamageTicks = i;
    }

    @Override
    public double getLastDamage() {
        if (lastDamage != null) return lastDamage;
        if (lastDamageCause != null) return lastDamageCause.getDamage();
        return 0;
    }

    @Override
    public void setLastDamage(double v) {
        lastDamage = v;
    }

    @Override
    public int getNoDamageTicks() {
        return noDamageTicks;
    }

    @Override
    public void setNoDamageTicks(int i) {
        this.noDamageTicks = i;
    }

    @Nullable
    @Override
    public Player getKiller() {
        return null;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect potionEffect) {
        PotionEffectType type = potionEffect.getType();
        PotionEffect old = potionEffects.get(type);
        if (old == null
            || old.getDuration() > potionEffect.getDuration()
            || potionEffect.getAmplifier() > old.getAmplifier()) {
            potionEffects.put(type, potionEffect);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect potionEffect, boolean b) {
        return potionEffects.put(potionEffect.getType(), potionEffect) == null;
    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> collection) {
        return collection.stream().anyMatch(this::addPotionEffect);
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType potionEffectType) {
        return potionEffects.containsKey(potionEffectType);
    }

    @Nullable
    @Override
    public PotionEffect getPotionEffect(@NotNull PotionEffectType potionEffectType) {
        return potionEffects.get(potionEffectType);
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType potionEffectType) {
        potionEffects.remove(potionEffectType);
    }

    @NotNull
    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return potionEffects.values();
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return removeWhenFarAway;
    }

    @Override
    public void setRemoveWhenFarAway(boolean b) {
        this.removeWhenFarAway = b;
    }

    @Nullable
    @Override
    public EntityEquipment getEquipment() {
        return inventory;
    }

    @Override
    public void setCanPickupItems(boolean b) {
        this.canPickupItems = b;
    }

    @Override
    public boolean getCanPickupItems() {
        return canPickupItems;
    }

    @Override
    public boolean isLeashed() {
        return leashHolder != null;
    }

    @NotNull
    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        if (leashHolder == null) return this;
        return leashHolder;
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity entity) {
        boolean changed = Objects.equals(entity, leashHolder);
        this.leashHolder = entity;
        return changed;
    }

    @Override
    public boolean isGliding() {
        return gliding;
    }

    @Override
    public void setGliding(boolean b) {
        this.gliding = true;
    }

    @Override
    public boolean isSwimming() {
        return swimming;
    }

    @Override
    public void setSwimming(boolean b) {
        this.swimming = true;
    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public void setAI(boolean b) {
    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public void attack(@NotNull Entity entity) {
    }

    @Override
    public void swingMainHand() {
    }

    @Override
    public void swingOffHand() {
    }

    @Override
    public void setCollidable(boolean b) {
        this.collidable = b;
    }

    @Override
    public boolean isCollidable() {
        return this.collidable;
    }

    @NotNull
    @Override
    public Set<UUID> getCollidableExemptions() {
        return Set.of();
    }

    @Nullable
    @Override
    public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return (T) memory.get(memoryKey);
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T t) {
        memory.put(memoryKey, t);
    }

    @Nullable
    @Override
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributes.get(attribute);
    }

    @Override
    public void damage(double v) {
        health -= v;
        lastDamageCause = new EntityDamageEvent(this, EntityDamageEvent.DamageCause.CUSTOM, v);
    }

    @Override
    public void damage(double v, @Nullable Entity entity) {
        health -= v;
        lastDamageCause = new EntityDamageByEntityEvent(entity, this, EntityDamageEvent.DamageCause.CUSTOM, v);
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public void setHealth(double v) {
        this.health = v;
    }

    @Override
    public double getAbsorptionAmount() {
        return absorptionAmount;
    }

    @Override
    public void setAbsorptionAmount(double v) {
        this.absorptionAmount = v;
    }

    @Override
    public double getMaxHealth() {
        return getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    @Override
    public void setMaxHealth(double v) {
        getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(v);
        getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers().clear();
    }

    @Override
    public void resetMaxHealth() {
        setMaxHealth(20);
    }

    @Nullable
    @Override
    public String getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(@Nullable String s) {
        this.customName = s;
    }

    @Override
    public void setMetadata(@NotNull String s, @NotNull MetadataValue metadataValue) {
        metadata.compute(s, (k, map) -> {
            if (map == null) map = new HashMap<>();
            map.put(metadataValue.getOwningPlugin(), metadataValue);
            return map;
        });
    }

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String s) {
        return List.copyOf(metadata.getOrDefault(s, Map.of()).values());
    }

    @Override
    public boolean hasMetadata(@NotNull String s) {
        return metadata.containsKey(s);
    }

    @Override
    public void removeMetadata(@NotNull String s, @NotNull Plugin plugin) {
        Map<Plugin, MetadataValue> v = metadata.get(s);
        if (v != null) {
            v.remove(plugin);
            if (v.isEmpty()) {
                metadata.remove(s);
            }
        }
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return permissible.isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return permissible.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return permissible.hasPermission(s);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return permissible.hasPermission(permission);
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        return permissible.addAttachment(plugin, s, b);
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return permissible.addAttachment(plugin);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        return permissible.addAttachment(plugin, s, b, i);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        return permissible.addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
        permissible.removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {
        permissible.recalculatePermissions();
    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return permissible.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return operator;
    }

    @Override
    public void setOp(boolean b) {
        this.operator = b;
    }

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        if (persistentDataContainer == null) persistentDataContainer = new FakePersistentDataContainer();
        return persistentDataContainer;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull String s, @NotNull byte[] bytes) {
    }

    @NotNull
    @Override
    public Set<String> getListeningPluginChannels() {
        return Set.of();
    }

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> aClass) {
        T entity = (T) getWorld().spawnEntity(getLocation(), projectileTypeFromClass(aClass));
        entity.setVelocity(getLocation().getDirection());
        return entity;
    }

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> aClass, @Nullable Vector vector) {
        T entity = (T) getWorld().spawnEntity(getLocation(), projectileTypeFromClass(aClass));
        entity.setVelocity(vector);
        return entity;
    }

    private static <P extends Projectile> EntityType projectileTypeFromClass(Class<? extends P> clazz) {
        if (Arrow.class.isAssignableFrom(clazz)) {
            return EntityType.ARROW;
        } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
            return EntityType.SPECTRAL_ARROW;
        } else if (ThrownPotion.class.isAssignableFrom(clazz)) {
            return EntityType.SPLASH_POTION;
        } else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
            return EntityType.THROWN_EXP_BOTTLE;
        } else if (Snowball.class.isAssignableFrom(clazz)) {
            return EntityType.SNOWBALL;
        } else if (DragonFireball.class.isAssignableFrom(clazz)) {
            return EntityType.DRAGON_FIREBALL;
        } else if (Egg.class.isAssignableFrom(clazz)) {
            return EntityType.EGG;
        } else if (EnderPearl.class.isAssignableFrom(clazz)) {
            return EntityType.ENDER_PEARL;
        } else if (Fireball.class.isAssignableFrom(clazz)) {
            return EntityType.FIREBALL;
        } else if (Firework.class.isAssignableFrom(clazz)) {
            return EntityType.FIREWORK;
        } else if (FishHook.class.isAssignableFrom(clazz)) {
            return EntityType.FISHING_HOOK;
        } else if (LlamaSpit.class.isAssignableFrom(clazz)) {
            return EntityType.LLAMA_SPIT;
        } else if (ShulkerBullet.class.isAssignableFrom(clazz)) {
            return EntityType.SHULKER_BULLET;
        } else if (Trident.class.isAssignableFrom(clazz)) {
            return EntityType.TRIDENT;
        } else if (WitherSkull.class.isAssignableFrom(clazz)) {
            return EntityType.WITHER_SKULL;
        }
        return EntityType.UNKNOWN;
    }
}
