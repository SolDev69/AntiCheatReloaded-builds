/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2021 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.check.packet;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.event.EventListener;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;

public final class BadPacketsCheck {

	public static void runCheck(final Player player, final PacketEvent event) {
		final Backend backend = AntiCheatReloaded.getManager().getBackend();
		// Confirm if we should even check for BadPackets
		if (!AntiCheatReloaded.getManager().getCheckManager().willCheck(player, CheckType.BADPACKETS)
				|| backend.isMovingExempt(player) || player.isDead()) {
			return;
		}

		final User user = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId());
		final PacketContainer packet = event.getPacket();
		final float pitch = packet.getFloat().read(1);
		// Check for derp
		if (Math.abs(pitch) > 90) {
			flag(player, event, "had an illegal pitch");
			return;
		}

		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final double tps = AntiCheatReloaded.getPlugin().getTPS();
		final MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager()
				.getUser(player.getUniqueId()).getMovementManager();
		if (user.isLagging() || tps < checksConfig.getDouble(CheckType.BADPACKETS, "minimumTps")
				|| (System.currentTimeMillis() - movementManager.lastTeleport <= checksConfig
						.getInteger(CheckType.BADPACKETS, "teleportCompensation"))) {
			return;
		}

		final double x = packet.getDoubles().read(0);
		final double y = packet.getDoubles().read(1);
		final double z = packet.getDoubles().read(2);
		final float yaw = packet.getFloat().read(0);
		// Create location from new data
		final Location previous = player.getLocation();
		final Location current = new Location(previous.getWorld(), x, y, z, yaw, pitch);
		final double distance = previous.distanceSquared(current);
		double maxDistance = checksConfig.getDouble(CheckType.BADPACKETS, "maxDistance");
		// Fix falling false
		if (movementManager.airTicks >= 40 && movementManager.motionY < 0 && movementManager.lastMotionY < 0) {
			maxDistance *= 1.5D;
		}
		
		if (distance > maxDistance) {
			flag(player, event,
					"moved too far between packets (distance="
							+ new BigDecimal(distance).setScale(1, RoundingMode.HALF_UP) + ", max=" + maxDistance
							+ ", at=" + movementManager.airTicks + ")");
			return;
		}
	}

	private static void flag(final Player player, final PacketEvent event, final String message) {
		event.setCancelled(true);
		// We are currently not in the main server thread, so switch
		AntiCheatReloaded.sendToMainThread(new Runnable() {
			@Override
			public void run() {
				EventListener.log(new CheckResult(CheckResult.Result.FAILED, message).getMessage(), player,
						CheckType.BADPACKETS, null);
				player.teleport(player.getLocation());
			}
		});
	}

}
