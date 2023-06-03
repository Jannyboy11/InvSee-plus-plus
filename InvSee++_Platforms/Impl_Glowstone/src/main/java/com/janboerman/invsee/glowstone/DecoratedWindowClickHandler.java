package com.janboerman.invsee.glowstone;

import com.flowpowered.network.MessageHandler;
import com.janboerman.invsee.spigot.api.SpectatorInventoryView;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.net.GlowSession;
import net.glowstone.net.handler.play.inv.WindowClickHandler;
import net.glowstone.net.message.play.inv.WindowClickMessage;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DecoratedWindowClickHandler implements MessageHandler<GlowSession, WindowClickMessage> {

    private final MessageHandler<GlowSession, WindowClickMessage> delegate;

    public DecoratedWindowClickHandler() {
        this(new WindowClickHandler());
    }

    DecoratedWindowClickHandler(MessageHandler<GlowSession, WindowClickMessage> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void handle(GlowSession glowSession, WindowClickMessage windowClickMessage) {
        GlowPlayer player = glowSession.getPlayer();
        InventoryView openInventory = player.getOpenInventory();

        SpectatorInventoryView view = null;
        List<ItemStack> before = null, after = null;

        if (openInventory instanceof SpectatorInventoryView) {
            view = (SpectatorInventoryView) openInventory;
            before = Arrays.stream(view.getTopInventory().getContents())
                    .map(ItemStack::clone)
                    .collect(Collectors.toList());
        }

        delegate.handle(glowSession, windowClickMessage);

        if (openInventory instanceof SpectatorInventoryView) {
            after = Arrays.stream(view.getTopInventory().getContents())
                    .map(ItemStack::clone)
                    .collect(Collectors.toList());
        }

        if (view instanceof MainInventoryView) {
            ((MainInventoryView) view).onClick(before, after);
        } else if (view instanceof EnderInventoryView) {
            ((EnderInventoryView) view).onClick(before, after);
        }
    }
}
