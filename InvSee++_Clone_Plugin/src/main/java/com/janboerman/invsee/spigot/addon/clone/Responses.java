package com.janboerman.invsee.spigot.addon.clone;

import com.janboerman.invsee.spigot.api.response.ImplementationFault;
import com.janboerman.invsee.spigot.api.response.NotCreatedReason;
import com.janboerman.invsee.spigot.api.response.OfflineSupportDisabled;
import com.janboerman.invsee.spigot.api.response.TargetDoesNotExist;
import com.janboerman.invsee.spigot.api.response.TargetHasExemptPermission;
import com.janboerman.invsee.spigot.api.response.UnknownTarget;
import com.janboerman.invsee.spigot.api.target.Target;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

final class Responses {

    private Responses() {
    }

    static void sendInventoryError(CommandSender to, Target target, NotCreatedReason reason) {
        if (reason instanceof TargetDoesNotExist) {
            to.sendMessage(ChatColor.RED + "Player " + target + " does not exist.");
        } else if (reason instanceof UnknownTarget) {
            to.sendMessage(ChatColor.RED + "Player " + target + " has not logged onto the server yet.");
        }  else if (reason instanceof TargetHasExemptPermission) {
            to.sendMessage(ChatColor.RED + "Player " + target + " is exempted from being spectated.");
        } else if (reason instanceof ImplementationFault) {
            to.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + target + "'s inventory.");
        } else if (reason instanceof OfflineSupportDisabled) {
            to.sendMessage(ChatColor.RED + "Spectating offline players' inventories is disabled.");
        } else {
            to.sendMessage(ChatColor.RED + "Could not create " + target + "'s inventory for an unknown reason.");
        }
    }

    static void sendEnderChestError(CommandSender to, Target target, NotCreatedReason reason) {
        if (reason instanceof TargetDoesNotExist) {
            to.sendMessage(ChatColor.RED + "Player " + target + " does not exist.");
        } else if (reason instanceof UnknownTarget) {
            to.sendMessage(ChatColor.RED + "Player " + target + " has not logged onto the server yet.");
        }  else if (reason instanceof TargetHasExemptPermission) {
            to.sendMessage(ChatColor.RED + "Player " + target + " is exempted from being spectated.");
        } else if (reason instanceof ImplementationFault) {
            to.sendMessage(ChatColor.RED + "An internal fault occurred when trying to load " + target + "'s enderchest.");
        } else if (reason instanceof OfflineSupportDisabled) {
            to.sendMessage(ChatColor.RED + "Spectating offline players' enderchests is disabled.");
        } else {
            to.sendMessage(ChatColor.RED + "Could not create " + target + "'s enderchest for an unknown reason.");
        }
    }
}
