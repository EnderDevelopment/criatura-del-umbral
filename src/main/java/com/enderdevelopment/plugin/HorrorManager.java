package com.enderdevelopment.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class HorrorManager implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey markerKey;
    private final Map<UUID, Long> resetDays = new HashMap<>();
    private BukkitTask task;

    public HorrorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.markerKey = new NamespacedKey(plugin, "errorworld_entity");
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        removeMarkedEntities();
    }

    public void resetWorlds() {
        for (World world : Bukkit.getWorlds()) {
            resetDays.put(world.getUID(), world.getFullTime() / 24000L);
        }
        removeMarkedEntities();
    }

    public String statusMessage() {
        StringBuilder status = new StringBuilder("ErrorWorld phases:");
        for (World world : Bukkit.getWorlds()) {
            status.append(" ").append(world.getName()).append("=").append(phase(world));
        }
        return status.toString();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode().isInvulnerable()) {
                continue;
            }
            int phase = phase(player.getWorld());
            if (phase == 1) {
                dayOne(player);
            } else if (phase == 2) {
                dayTwo(player);
            } else {
                dayThree(player);
            }
        }
    }

    private int phase(World world) {
        long day = world.getFullTime() / 24000L;
        long initialDay = resetDays.computeIfAbsent(world.getUID(), ignored -> day);
        return (int) Math.min(3L, day - initialDay + 1L);
    }

    private void dayOne(Player player) {
        spawnFog(player, 30);
        if (player.getWorld().getTime() % 200L < 20L) {
            placeWarningSign(player);
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.45f, 0.6f);
        }
    }

    private void dayTwo(Player player) {
        spawnFog(player, 30);
        if (player.getWorld().getTime() % 100L < 20L) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.7f, 0.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.25f, 0.7f);
        }
        if (player.getWorld().getTime() % 300L < 20L) {
            spawnWatcher(player);
        }
    }

    private void dayThree(Player player) {
        spawnFog(player, 30);
        targetExistingWatchers(player);
        if (player.getWorld().getTime() % 160L < 20L) {
            spawnWatcher(player);
        }
    }

    private void targetExistingWatchers(Player player) {
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 40, 20, 40)) {
            if (entity instanceof WitherSkeleton watcher
                    && watcher.getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE)) {
                watcher.setTarget(player);
                showWhiteEyes(watcher);
            }
        }
    }

    private void spawnFog(Player player, int radius) {
        Location center = player.getLocation().add(0, 1.0, 0);
        player.spawnParticle(Particle.CLOUD, center, 16, radius / 2.0, 1.2, radius / 2.0, 0.01);
        player.spawnParticle(Particle.ASH, center, 8, radius / 2.0, 1.0, radius / 2.0, 0.005);
        if (player.getWorld().getTime() % 80L < 20L) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, 40, 0, false, false, false));
        }
    }

    private void placeWarningSign(Player player) {
        Location target = safeSignLocation(player);
        if (target == null || target.getBlock().getType() != Material.AIR) {
            return;
        }
        target.getBlock().setType(Material.DARK_OAK_SIGN);
        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) target.getBlock().getState();
        sign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(0, "THIS WORLD");
        sign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(1, "SHOULD NOT");
        sign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(2, "BE HERE");
        sign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(3, "ERROR: DAY 1");
        sign.update(true, false);
    }

    private Location safeSignLocation(Player player) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        if (direction.lengthSquared() == 0) {
            direction = new Vector(1, 0, 0);
        }
        Location location = player.getLocation().add(direction.multiply(5));
        location.setY(player.getLocation().getBlockY());
        return location;
    }

    private void spawnWatcher(Player player) {
        Location location = findWatcherLocation(player);
        if (location == null) {
            return;
        }
        WitherSkeleton watcher = (WitherSkeleton) player.getWorld().spawnEntity(location, EntityType.WITHER_SKELETON);
        watcher.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        watcher.setCustomName("The Error");
        watcher.setCustomNameVisible(false);
        watcher.setRemoveWhenFarAway(true);
        watcher.setCanPickupItems(false);
        watcher.getEquipment().setHelmet(blackArmor(Material.LEATHER_HELMET));
        watcher.getEquipment().setChestplate(blackArmor(Material.LEATHER_CHESTPLATE));
        watcher.getEquipment().setLeggings(blackArmor(Material.LEATHER_LEGGINGS));
        watcher.getEquipment().setBoots(blackArmor(Material.LEATHER_BOOTS));
        watcher.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
        watcher.getEquipment().setHelmetDropChance(0);
        watcher.getEquipment().setChestplateDropChance(0);
        watcher.getEquipment().setLeggingsDropChance(0);
        watcher.getEquipment().setBootsDropChance(0);
        watcher.getEquipment().setItemInMainHandDropChance(0);
        watcher.setTarget(phase(player.getWorld()) >= 3 ? player : null);
        showWhiteEyes(watcher);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.18f, 1.8f);
    }

    private void showWhiteEyes(WitherSkeleton watcher) {
        Location head = watcher.getEyeLocation();
        Vector forward = watcher.getLocation().getDirection().setY(0).normalize();
        if (forward.lengthSquared() == 0) {
            forward = new Vector(1, 0, 0);
        }
        Vector side = new Vector(-forward.getZ(), 0, forward.getX()).normalize().multiply(0.13);
        Location eyes = head.add(forward.multiply(0.28)).add(0, -0.05, 0);
        watcher.getWorld().spawnParticle(Particle.END_ROD, eyes.clone().add(side), 1, 0, 0, 0, 0);
        watcher.getWorld().spawnParticle(Particle.END_ROD, eyes.clone().subtract(side), 1, 0, 0, 0, 0);
    }

    private ItemStack blackArmor(Material material) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.BLACK);
        item.setItemMeta(meta);
        return item;
    }

    private Location findWatcherLocation(Player player) {
        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        if (forward.lengthSquared() == 0) {
            forward = new Vector(1, 0, 0);
        }
        Location location = player.getLocation().clone().add(forward.multiply(10));
        location.setY(player.getLocation().getBlockY());
        if (!location.getBlock().isPassable() || !location.clone().add(0, 1, 0).getBlock().isPassable()) {
            return null;
        }
        return location;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("The world has an error.");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton watcher)) {
            return;
        }
        PersistentDataContainer data = watcher.getPersistentDataContainer();
        if (data.has(markerKey, PersistentDataType.BYTE)) {
            event.setDamage(event.getDamage() * 0.5);
        }
    }

    private void removeMarkedEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE)) {
                    entity.remove();
                }
            }
        }
    }
}
