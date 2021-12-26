package com.janboerman.invsee.spigot.api.target;

import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerTarget implements Target {

    private final Player player;

    public PlayerTarget(Player player) {
        this.player = Objects.requireNonNull(player);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return getPlayer().getName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(player);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PlayerTarget)) return false;

        PlayerTarget that = (PlayerTarget) obj;
        return Objects.equals(this.getPlayer(), that.getPlayer());
    }

}
