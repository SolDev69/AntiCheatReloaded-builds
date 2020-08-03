/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2020 Rammelkast
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
package com.rammelkast.anticheatreloaded.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

public class KillAuraCheck {

	// Angle check
	public static final Map<UUID, Integer> ANGLE_FLAGS = new HashMap<UUID, Integer>();
	// PacketOrder check
	public static final Map<UUID, Integer> PACKETORDER_FLAGS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult checkReach(Player player, Entity target) {
		if (!(target instanceof LivingEntity))
			return PASS;
		User user = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId());
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();

		// Check if enabled
		if (!checksConfig.isSubcheckEnabled(CheckType.KILLAURA, "reach"))
			return PASS;

		double allowedReach = target.getVelocity().length() < 0.08
				? checksConfig.getDouble(CheckType.KILLAURA, "reach", "baseMaxValue.normal")
				: checksConfig.getDouble(CheckType.KILLAURA, "reach", "baseMaxValue.velocitized");
		// Lag compensation
		double lagExtraReach = checksConfig.getDouble(CheckType.KILLAURA, "reach", "lagCompensation.lagExtraReach");
		double pingCompensation = checksConfig.getDouble(CheckType.KILLAURA, "reach",
				"lagCompensation.pingCompensation");
		allowedReach += user.getPing() * pingCompensation;
		if (user.isLagging())
			allowedReach += lagExtraReach;
		if (target instanceof Player) {
			User targetUser = AntiCheatReloaded.getManager().getUserManager().getUser(target.getUniqueId());
			allowedReach += targetUser.getPing() * pingCompensation;
			if (targetUser.isLagging())
				allowedReach += lagExtraReach;
		}
		// Velocity compensation
		double velocityMultiplier = checksConfig.getDouble(CheckType.KILLAURA, "reach", "velocityMultiplier");
		allowedReach += Math.abs(target.getVelocity().length()) * velocityMultiplier;
		double reachedDistance = ((LivingEntity) target).getLocation().toVector()
				.distance(player.getLocation().toVector());
		if (reachedDistance > allowedReach)
			return new CheckResult(CheckResult.Result.FAILED,
					"reached too far (distance=" + Utilities.roundDouble(reachedDistance, 6) + ", max=" + Utilities.roundDouble(allowedReach, 6) + ")");
		return PASS;
	}

	public static CheckResult checkAngle(Player player, EntityDamageEvent event) {
		UUID uuid = player.getUniqueId();
		Entity entity = event.getEntity();
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();

		// Check if enabled
		if (!checksConfig.isSubcheckEnabled(CheckType.KILLAURA, "angle"))
			return PASS;

		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			Location eyeLocation = player.getEyeLocation();

			double yawDifference = calculateYawDifference(eyeLocation, living.getLocation());
			double playerYaw = player.getEyeLocation().getYaw();

			double angleDifference = Math.abs(180 - Math.abs(Math.abs(yawDifference - playerYaw) - 180));
			int maxDifference = checksConfig.getInteger(CheckType.KILLAURA, "angle", "maxDifference");
			if (Math.round(angleDifference) > maxDifference) {
				if (!ANGLE_FLAGS.containsKey(uuid)) {
					ANGLE_FLAGS.put(uuid, 1);
					return PASS;
				}

				int flags = ANGLE_FLAGS.get(uuid);
				int vlBeforeFlag = checksConfig.getInteger(CheckType.KILLAURA, "angle", "vlBeforeFlag");
				if (flags >= vlBeforeFlag) {
					ANGLE_FLAGS.remove(uuid);
					return new CheckResult(CheckResult.Result.FAILED,
							"tried to attack from an illegal angle (angle=" + Math.round(angleDifference) + ")");
				}

				ANGLE_FLAGS.put(uuid, flags + 1);
			}
		}
		
		return PASS;
	}

	public static CheckResult checkPacketOrder(Player player, Entity entity) {
		UUID uuid = player.getUniqueId();
		User user = AntiCheatReloaded.getManager().getUserManager().getUser(uuid);
		MovementManager movementManager = user.getMovementManager();
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();

		// Check if enabled
		if (!checksConfig.isSubcheckEnabled(CheckType.KILLAURA, "packetOrder"))
			return PASS;
		
		if (user.isLagging() || (System.currentTimeMillis() - movementManager.lastTeleport) <= 100
				|| AntiCheatReloaded.getPlugin().getTPS() < checksConfig.getDouble(CheckType.KILLAURA, "packetOrder", "minimumTps"))
			return PASS;

		long elapsed = System.currentTimeMillis() - movementManager.lastUpdate;
		if (elapsed < checksConfig.getInteger(CheckType.KILLAURA, "packetOrder", "minElapsedTime")) {
			if (!PACKETORDER_FLAGS.containsKey(uuid)) {
				PACKETORDER_FLAGS.put(uuid, 1);
				return PASS;
			}

			int flags = PACKETORDER_FLAGS.get(uuid);
			int vlBeforeFlag = checksConfig.getInteger(CheckType.KILLAURA, "packetOrder", "vlBeforeFlag");
			if (flags >= vlBeforeFlag) {
				PACKETORDER_FLAGS.remove(uuid);
				return new CheckResult(Result.FAILED, "suspicious packet order (elapsed=" + elapsed + ")");
			}

			PACKETORDER_FLAGS.put(uuid, flags + 1);
		}
		return PASS;
	}
	
	public static double calculateYawDifference(Location from, Location to) {
		Location clonedFrom = from.clone();
		Vector startVector = clonedFrom.toVector();
		Vector targetVector = to.toVector();
		clonedFrom.setDirection(targetVector.subtract(startVector));
		return clonedFrom.getYaw();
	}

}
