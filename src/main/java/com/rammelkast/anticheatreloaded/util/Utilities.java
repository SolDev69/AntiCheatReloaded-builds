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

package com.rammelkast.anticheatreloaded.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

public final class Utilities {
	private static final List<Material> INSTANT_BREAK = new ArrayList<Material>();
	private static final List<Material> FOOD = new ArrayList<Material>();
	private static final List<Material> CLIMBABLE = new ArrayList<Material>();
	private static final Map<Material, Material> COMBO = new HashMap<Material, Material>();
	
	public static final Material LILY_PAD;
	public static final Material COB_WEB;

	public static final double JUMP_MOTION_Y = 0.41999998688697815;

	/**
	 * Check if only the block beneath them is standable (includes water + lava)
	 * 
	 * @param block the block to check (under)
	 * @return true if they cannot stand there
	 */
	public static boolean cantStandAtSingle(Block block) {
		Block otherBlock = block.getLocation().add(0, -0.5, 0).getBlock();
		boolean center = otherBlock.getType() == Material.AIR;
		return center;
	}

	/**
	 * Determine whether a player cannot stand on or around the given block
	 *
	 * @param block the block to check
	 * @return true if the player should be unable to stand here
	 */
	public static boolean cantStandAt(Block block) {
		return !canStand(block) && cantStandClose(block) && cantStandFar(block);
	}

	/**
	 * Determine whether a player should be unable to stand at a given location
	 *
	 * @param location the location to check
	 * @return true if the player should be unable to stand here
	 */
	public static boolean cantStandAtExp(Location location) {
		return cantStandAt(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()).getBlock());
	}

	/**
	 * Determine whether cannot stand on the block's immediately surroundings
	 * (North, East, South, West)
	 *
	 * @param block the block to check
	 * @return true if a player cannot stand in the immediate vicinity
	 */
	public static boolean cantStandClose(Block block) {
		return !canStand(block.getRelative(BlockFace.NORTH)) && !canStand(block.getRelative(BlockFace.EAST))
				&& !canStand(block.getRelative(BlockFace.SOUTH)) && !canStand(block.getRelative(BlockFace.WEST));
	}

	/**
	 * Determine whether cannot stand on the block's outer surroundings
	 *
	 * @param block the block to check
	 * @return true if a player cannot stand in areas further away from the block
	 */
	public static boolean cantStandFar(Block block) {
		return !canStand(block.getRelative(BlockFace.NORTH_WEST)) && !canStand(block.getRelative(BlockFace.NORTH_EAST))
				&& !canStand(block.getRelative(BlockFace.SOUTH_WEST))
				&& !canStand(block.getRelative(BlockFace.SOUTH_EAST));
	}

	/**
	 * Determine whether a player can stand on the given block
	 *
	 * @param block the block to check
	 * @return true if the player can stand here
	 */
	public static boolean canStand(Block block) {
		return !(block.isLiquid() || block.getType() == Material.AIR);
	}

	public static boolean isNotNearSlime(Block block) {
		return !isSlime(block.getRelative(BlockFace.NORTH)) && !isSlime(block.getRelative(BlockFace.EAST))
				&& !isSlime(block.getRelative(BlockFace.SOUTH)) && !isSlime(block.getRelative(BlockFace.WEST))
				&& !isSlime(block.getRelative(BlockFace.DOWN));
	}

	public static boolean couldBeOnBoat(Player player) {
		return couldBeOnBoat(player, 0.35, false);
	}

	public static boolean couldBeOnBoat(Player player, double range, boolean checkY) {
		for (Entity entity : player.getNearbyEntities(range, range, range)) {
			if (entity instanceof Boat) {
				if (((Boat) entity).getLocation().getY() < player.getLocation().getY() + 0.25) {
					return true;
				} else if (!checkY) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determine whether a player could be standing on ice
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on ice
	 */
	public static boolean couldBeOnIce(Location location) {
		return isNearIce(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()))
				|| isNearIce(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.26D,
						location.getBlockZ()));
	}

	/**
	 * Determine whether a block is a type of ice
	 *
	 * @param block block to check
	 * @return true if block is a type of ice
	 */
	public static boolean isIce(Block block) {
		Material type = block.getType();
		return type.name().endsWith("ICE");
	}

	public static boolean isNearIce(Location location) {
		return isIce(location.getBlock()) || isIce(location.getBlock().getRelative(BlockFace.NORTH))
				|| isIce(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isIce(location.getBlock().getRelative(BlockFace.EAST))
				|| isIce(location.getBlock().getRelative(BlockFace.WEST))
				|| isIce(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isIce(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isIce(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isIce(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	/**
	 * Determine whether a block is a shulker box
	 *
	 * @param block block to check
	 * @return true if block is a shulker box
	 */
	public static boolean isShulkerBox(Block block) {
		Material type = block.getType();
		return type.name().endsWith("SHULKER_BOX");
	}

	public static boolean isNearShulkerBox(Location location) {
		if (VersionUtil.isBountifulUpdate())
			return false;
		return isShulkerBox(location.getBlock()) || isShulkerBox(location.getBlock().getRelative(BlockFace.NORTH))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.EAST))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.WEST))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isShulkerBox(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	/**
	 * Determine whether a player could be standing on a halfblock
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on a halfblock
	 */
	public static boolean couldBeOnHalfblock(Location location) {
		return isNearHalfblock(
				new Location(location.getWorld(), location.getX(), location.getY() - 0.01D, location.getBlockZ()))
				|| isNearHalfblock(new Location(location.getWorld(), location.getX(), location.getY() - 0.51D,
						location.getBlockZ()));
	}

	public static boolean isNearHalfblock(Location location) {
		return isHalfblock(location.getBlock()) || isHalfblock(location.getBlock().getRelative(BlockFace.NORTH))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.WEST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	public static boolean isHalfblock(Block block) {
		if (!VersionUtil.isBountifulUpdate()) {
			BoundingBox box = block.getBoundingBox();
			double height = box.getMaxY() - box.getMinY();
			if (height > 0.42 && height <= 0.6 && block.getType().isSolid())
				return true;
		}
		return isSlab(block) || isStair(block) || isWall(block) || block.getType() == Material.SNOW
				|| block.getType().name().endsWith("HEAD");
	}

	/**
	 * Determine whether a player could be standing on slime
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on slime
	 */
	public static boolean couldBeOnSlime(Location location) {
		return isNearSlime(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()))
				|| isNearSlime(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.51D,
						location.getBlockZ()));
	}

	/**
	 * Determine whether a block is a type of slime
	 *
	 * @param block block to check
	 * @return true if block is a type of slime
	 */
	public static boolean isSlime(Block block) {
		Material type = block.getType();
		return type.equals(XMaterial.SLIME_BLOCK.parseMaterial());
	}

	public static boolean isNearSlime(Location location) {
		return isSlime(location.getBlock()) || isSlime(location.getBlock().getRelative(BlockFace.NORTH))
				|| isSlime(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isSlime(location.getBlock().getRelative(BlockFace.EAST))
				|| isSlime(location.getBlock().getRelative(BlockFace.WEST))
				|| isSlime(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isSlime(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isSlime(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isSlime(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	/**
	 * Determine whether a player is fully submerged in water
	 *
	 * @param player the player's location
	 * @return true if the player is fully in the water
	 */
	public static boolean isFullyInWater(Location player) {
		double touchedX = fixXAxis(player.getX());

		// Yes, this doesn't make sense, but it's supposed to fix some false positives
		// in water walk.
		// Think of it as 2 negatives = a positive :)
		if (!(new Location(player.getWorld(), touchedX, player.getY(), player.getBlockZ()).getBlock()).isLiquid()
				&& !(new Location(player.getWorld(), touchedX, Math.round(player.getY()), player.getBlockZ())
						.getBlock()).isLiquid()) {
			return true;
		}

		return (new Location(player.getWorld(), touchedX, player.getY(), player.getBlockZ()).getBlock()).isLiquid()
				&& (new Location(player.getWorld(), touchedX, Math.round(player.getY()), player.getBlockZ()).getBlock())
						.isLiquid();
	}

	/**
	 * Fixes a player's X position to determine the block they are on, even if
	 * they're on the edge
	 *
	 * @param x player's x position
	 * @return fixed x position
	 */
	public static double fixXAxis(double x) {
		/* For Z axis, just use Math.round(xaxis); */
		double touchedX = x;
		double rem = touchedX - Math.round(touchedX) + 0.01D;
		if (rem < 0.30D) {
			touchedX = NumberConversions.floor(x) - 1;
		}
		return touchedX;
	}

	/**
	 * Determine if the player is hovering over water with the given limit
	 *
	 * @param player the player's location
	 * @param blocks max blocks to check
	 * @return true if the player is hovering over water
	 */
	public static boolean isHoveringOverWater(Location player, int blocks) {
		for (int i = player.getBlockY(); i > player.getBlockY() - blocks; i--) {
			Block newloc = (new Location(player.getWorld(), player.getBlockX(), i, player.getBlockZ())).getBlock();
			if (newloc.getType() != Material.AIR) {
				return newloc.isLiquid();
			}
		}

		return false;
	}

	/**
	 * Determine if the player is hovering over water with a hard limit of 25 blocks
	 *
	 * @param player the player's location
	 * @return true if the player is hovering over water
	 */
	public static boolean isHoveringOverWater(Location player) {
		return isHoveringOverWater(player, 25);
	}

	/**
	 * Determine whether a material will break instantly when hit
	 *
	 * @param m the material to check
	 * @return true if the material is instant break
	 */
	public static boolean isInstantBreak(Material m) {
		return INSTANT_BREAK.contains(m);
	}

	/**
	 * Determine whether a material is edible
	 *
	 * @param m the material to check
	 * @return true if the material is food
	 */
	public static boolean isFood(Material m) {
		return FOOD.contains(m);
	}

	/**
	 * Determine whether a block is a slab
	 *
	 * @param block block to check
	 * @return true if slab
	 */
	public static boolean isSlab(Block block) {
		Material type = block.getType();
		return type.name().endsWith("SLAB");
	}

	/**
	 * Determine whether a block is a bed
	 *
	 * @param block block to bed
	 * @return true if bed
	 */
	public static boolean isBed(Block block) {
		Material type = block.getType();
		return type.name().endsWith("BED");
	}

	public static boolean isNearBed(Location location) {
		return isBed(location.getBlock()) || isBed(location.getBlock().getRelative(BlockFace.NORTH))
				|| isBed(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isBed(location.getBlock().getRelative(BlockFace.EAST))
				|| isBed(location.getBlock().getRelative(BlockFace.WEST))
				|| isBed(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isBed(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isBed(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isBed(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	/**
	 * Determine whether a block is a stair
	 *
	 * @param block block to check
	 * @return true if stair
	 */
	public static boolean isStair(Block block) {
		Material type = block.getType();
		return type.name().endsWith("STAIRS");
	}

	/**
	 * Determine whether a block is a wall
	 *
	 * @param block block to check
	 * @return true if wall
	 */
	public static boolean isWall(Block block) {
		Material type = block.getType();
		return type.name().endsWith("WALL") || type.name().endsWith("FENCE");
	}

	/**
	 * Determine whether a player is sprinting or flying
	 *
	 * @param player player to check
	 * @return true if sprinting or flying
	 */
	public static boolean sprintFly(Player player) {
		return player.isSprinting() || player.isFlying();
	}

	/**
	 * Determine whether a player is standing on a lily pad
	 *
	 * @param player player to check
	 * @return true if on lily pad
	 */
	public static boolean isOnLilyPad(Player player) {
		Block block = player.getLocation().getBlock();
		return block.getType() == LILY_PAD || block.getRelative(BlockFace.NORTH).getType() == LILY_PAD
				|| block.getRelative(BlockFace.SOUTH).getType() == LILY_PAD
				|| block.getRelative(BlockFace.EAST).getType() == LILY_PAD
				|| block.getRelative(BlockFace.WEST).getType() == LILY_PAD;
	}

	/**
	 * Determine whether a player is fully submersed in liquid
	 *
	 * @param player player to check
	 * @return true if submersed
	 */
	public static boolean isSubmersed(Player player) {
		return player.getLocation().getBlock().isLiquid()
				&& player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid();
	}

	/**
	 * Determine whether a player is in water
	 *
	 * @param player player to check
	 * @return true if in water
	 */
	public static boolean isInWater(Player player) {
		return player.getLocation().getBlock().isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid()
				|| (!VersionUtil.isBountifulUpdate()
						&& (player.getLocation().getBlock().getType() == XMaterial.KELP_PLANT.parseMaterial()
								|| player.getLocation().getBlock().getRelative(BlockFace.UP)
										.getType() == XMaterial.KELP_PLANT.parseMaterial()
								|| player.getLocation().getBlock().getRelative(BlockFace.DOWN)
										.getType() == XMaterial.KELP_PLANT.parseMaterial())
						&& isNearWater(player));
	}

	/**
	 * Determine whether a player is near a liquid block
	 *
	 * @param player player to check
	 * @return true if near liquid block
	 */
	public static boolean isNearWater(Player player) {
		return player.getLocation().getBlock().isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.WEST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST).isLiquid();
	}

	/**
	 * Determine whether a player is surrounded by liquid blocks
	 *
	 * @param player player to check
	 * @return true if surrounded by liquid blocks
	 */
	public static boolean isSurroundedByWater(Player player) {
		Location location = player.getLocation().clone().subtract(0, 0.1, 0);
		return location.getBlock().isLiquid() && location.getBlock().getRelative(BlockFace.NORTH).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH).isLiquid()
				&& location.getBlock().getRelative(BlockFace.EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.WEST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.NORTH_EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.NORTH_WEST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH_EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH_WEST).isLiquid();
	}
	
	/**
	 * Determine whether a player is near a web
	 *
	 * @param player player to check
	 * @return true if near a web
	 */
	public static boolean isNearWeb(Player player) {
		return isWeb(player.getLocation().getBlock())
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.DOWN))
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.UP))
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.NORTH))
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.SOUTH))
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.EAST))
				|| isWeb(player.getLocation().getBlock().getRelative(BlockFace.WEST));
	}
	

	/**
	 * Determine whether a block is a web
	 *
	 * @param block block to check
	 * @return true if web
	 */
	public static boolean isWeb(Block block) {
		return block.getType() == COB_WEB;
	}

	/**
	 * Determine whether a player is in a web
	 *
	 * @param player player to check
	 * @return true if in web
	 */
	public static boolean isInWeb(Player player) {
		return player.getLocation().getBlock().getType() == COB_WEB
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == COB_WEB
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).getType() == COB_WEB;
	}

	/**
	 * Determine whether a player is near a climbable block
	 *
	 * @param player player to check
	 * @return true if near climbable block
	 */
	public static boolean isNearClimbable(Player player) {
		return isClimbableBlock(player.getLocation().getBlock())
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.DOWN))
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.UP))
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.NORTH))
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.SOUTH))
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.EAST))
				|| isClimbableBlock(player.getLocation().getBlock().getRelative(BlockFace.WEST));
	}

	/**
	 * Determine whether a location is near a climbable block
	 *
	 * @param location location to check
	 * @return true if near climbable block
	 */
	public static boolean isNearClimbable(Location location) {
		return isClimbableBlock(location.getBlock())
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.DOWN))
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.UP))
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.NORTH))
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.EAST))
				|| isClimbableBlock(location.getBlock().getRelative(BlockFace.WEST));
	}

	/**
	 * Determine whether a block is climbable
	 *
	 * @param block block to check
	 * @return true if climbable
	 */
	public static boolean isClimbableBlock(Block block) {
		return CLIMBABLE.contains(block.getType());
	}

	/**
	 * Determine whether a player is on a vine (can be free hanging)
	 *
	 * @param player to check
	 * @return true if on vine
	 */
	public static boolean isOnVine(Player player) {
		return player.getLocation().getBlock().getType() == XMaterial.VINE.parseMaterial();
	}

	/**
	 * Determine whether a String can be cast to an Integer
	 *
	 * @param string text to check
	 * @return true if int
	 */
	public static boolean isInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Determine whether a String can be cast to a Double
	 *
	 * @param string text to check
	 * @return true if double
	 */
	public static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Determine if a block ISN'T one of the specified types
	 *
	 * @param block     block to check
	 * @param materials array of possible materials
	 * @return true if the block isn't any of the materials
	 */
	public static boolean blockIsnt(Block block, Material[] materials) {
		Material type = block.getType();
		for (Material m : materials) {
			if (m == type) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if a block ISN'T one of the specified types
	 *
	 * @param block    block to check
	 * @param endTypes array of possible name endings
	 * @return true if the block isn't any of the materials
	 */
	public static boolean blockIsnt(Block block, String[] endTypes) {
		Material type = block.getType();
		for (String s : endTypes) {
			if (type.name().endsWith(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parse a COMMAND[] input to a set of commands to execute
	 *
	 * @param command input string
	 * @return parsed commands
	 */
	public static String[] getCommands(String command) {
		return command.replaceAll("COMMAND\\[", "").replaceAll("]", "").split(";");
	}

	/**
	 * Remove all whitespace from the given string to ready it for parsing
	 *
	 * @param string the string to parse
	 * @return string with whitespace removed
	 */
	public static String removeWhitespace(String string) {
		return string.replaceAll(" ", "");
	}

	/**
	 * Determine if a player has the given enchantment on their armor
	 *
	 * @param player player to check
	 * @param e      enchantment to check
	 * @return true if the armor has this enchantment
	 */
	public static boolean hasArmorEnchantment(Player player, Enchantment e) {
		for (ItemStack is : player.getInventory().getArmorContents()) {
			if (is != null && is.containsEnchantment(e)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a list with the given string for execution
	 *
	 * @param string the string to parse
	 * @return ArrayList with string
	 */
	public static ArrayList<String> stringToList(final String string) {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 364115444874638230L;
			{
				add(string);
			}
		};
	}

	/**
	 * Create a comma-delimited string from a list
	 *
	 * @param list the list to parse
	 * @return the list in a string format
	 */
	public static String listToCommaString(List<String> list) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			b.append(list.get(i));
			if (i < list.size() - 1) {
				b.append(",");
			}
		}
		return b.toString();
	}

	/**
	 * Parse a string in the format of "XdXhXmXs" to seconds
	 * 
	 * @param string The string to parse
	 * @return seconds
	 */
	public static long lifeToSeconds(String string) {
		if (string.equals("0") || string.equals(""))
			return 0;
		String[] lifeMatch = new String[] { "d", "h", "m", "s" };
		int[] lifeInterval = new int[] { 86400, 3600, 60, 1 };
		long seconds = 0L;

		for (int i = 0; i < lifeMatch.length; i++) {
			Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
			while (matcher.find()) {
				seconds += Integer.parseInt(matcher.group(1)) * lifeInterval[i];
			}

		}
		return seconds;
	}

	/**
	 * Rounds a float value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static float roundFloat(float value, int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).floatValue();
	}

	/**
	 * Rounds a double value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static double roundDouble(double value, int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	public static int floor(double value) {
		int rounded = (int) value;
		return value < rounded ? rounded - 1 : rounded;
	}

	public static boolean isHoneyBlock(Block block) {
		if (!VersionUtil.isOfVersion("v1_15")) {
			return false;
		}
		return block.getType() == XMaterial.HONEY_BLOCK.parseMaterial();
	}

	static {
		// Start 1.8.8
		if (VersionUtil.isBountifulUpdate()) {
			LILY_PAD = XMaterial.LILY_PAD.parseMaterial();
			COB_WEB = XMaterial.COBWEB.parseMaterial();

			// Start instant break materials
			INSTANT_BREAK.add(XMaterial.COMPARATOR.parseMaterial());
			INSTANT_BREAK.add(XMaterial.REPEATER.parseMaterial());
			INSTANT_BREAK.add(Material.TORCH);
			INSTANT_BREAK.add(XMaterial.REDSTONE_TORCH.parseMaterial());
			INSTANT_BREAK.add(Material.REDSTONE_WIRE);
			INSTANT_BREAK.add(Material.TRIPWIRE);
			INSTANT_BREAK.add(Material.TRIPWIRE_HOOK);
			INSTANT_BREAK.add(Material.FIRE);
			INSTANT_BREAK.add(Material.FLOWER_POT);
			INSTANT_BREAK.add(Material.TNT);
			INSTANT_BREAK.add(Material.SLIME_BLOCK);
			INSTANT_BREAK.add(Material.CARROT);
			INSTANT_BREAK.add(Material.DEAD_BUSH);
			INSTANT_BREAK.add(Material.GRASS);
			INSTANT_BREAK.add(XMaterial.TALL_GRASS.parseMaterial());
			INSTANT_BREAK.add(LILY_PAD);
			INSTANT_BREAK.add(Material.MELON_STEM);
			INSTANT_BREAK.add(Material.MELON_STEM);
			INSTANT_BREAK.add(Material.BROWN_MUSHROOM);
			INSTANT_BREAK.add(Material.RED_MUSHROOM);
			INSTANT_BREAK.add(XMaterial.NETHER_WART.parseMaterial());
			INSTANT_BREAK.add(Material.POTATO);
			INSTANT_BREAK.add(Material.PUMPKIN_STEM);
			INSTANT_BREAK.add(Material.PUMPKIN_STEM);
			INSTANT_BREAK.add(XMaterial.OAK_SAPLING.parseMaterial());
			INSTANT_BREAK.add(Material.SUGAR_CANE);
			INSTANT_BREAK.add(Material.WHEAT);
			// End instant break materials

			// Start food
			FOOD.add(Material.APPLE);
			FOOD.add(Material.BAKED_POTATO);
			FOOD.add(Material.BREAD);
			FOOD.add(Material.CAKE);
			FOOD.add(Material.CARROT);
			FOOD.add(Material.COOKED_CHICKEN);
			FOOD.add(XMaterial.COOKED_COD.parseMaterial());
			FOOD.add(XMaterial.COD.parseMaterial());
			FOOD.add(Material.COOKED_MUTTON);
			FOOD.add(XMaterial.COOKED_PORKCHOP.parseMaterial());
			FOOD.add(Material.COOKED_RABBIT);
			FOOD.add(Material.COOKIE);
			FOOD.add(Material.GOLDEN_APPLE);
			FOOD.add(Material.GOLDEN_CARROT);
			FOOD.add(XMaterial.GLISTERING_MELON_SLICE.parseMaterial());
			FOOD.add(XMaterial.MUSHROOM_STEM.parseMaterial());
			FOOD.add(Material.POISONOUS_POTATO);
			FOOD.add(Material.POTATO);
			FOOD.add(Material.PUMPKIN_PIE);
			FOOD.add(Material.RABBIT_STEW);
			FOOD.add(Material.COOKED_BEEF);
			FOOD.add(XMaterial.BEEF.parseMaterial());
			FOOD.add(Material.COOKED_CHICKEN);
			FOOD.add(XMaterial.CHICKEN.parseMaterial());
			FOOD.add(Material.MUTTON);
			FOOD.add(XMaterial.PORKCHOP.parseMaterial());
			FOOD.add(Material.RABBIT);
			FOOD.add(Material.COOKED_RABBIT);
			FOOD.add(Material.ROTTEN_FLESH);
			FOOD.add(Material.SPIDER_EYE);

			// Start combos
			COMBO.put(Material.SHEARS, XMaterial.WHITE_WOOL.parseMaterial());

			COMBO.put(Material.IRON_SWORD, COB_WEB);
			COMBO.put(Material.DIAMOND_SWORD, COB_WEB);
			COMBO.put(Material.STONE_SWORD, COB_WEB);
			COMBO.put(XMaterial.WOODEN_SWORD.parseMaterial(), COB_WEB);
			// End combos

			// Start climbable
			CLIMBABLE.add(Material.VINE);
			CLIMBABLE.add(Material.LADDER);
			CLIMBABLE.add(Material.WATER);
			// End climbable
		}
		// End 1.8.8
		// Start other version
		else {
			LILY_PAD = Material.LILY_PAD;
			COB_WEB = Material.COBWEB;

			// Start instant break materials
			INSTANT_BREAK.add(Material.COMPARATOR);
			INSTANT_BREAK.add(Material.REPEATER);
			INSTANT_BREAK.add(Material.TORCH);
			INSTANT_BREAK.add(Material.REDSTONE_TORCH);
			INSTANT_BREAK.add(Material.REDSTONE_WIRE);
			INSTANT_BREAK.add(Material.TRIPWIRE);
			INSTANT_BREAK.add(Material.TRIPWIRE_HOOK);
			INSTANT_BREAK.add(Material.FIRE);
			INSTANT_BREAK.add(Material.FLOWER_POT);
			INSTANT_BREAK.add(Material.INFESTED_CHISELED_STONE_BRICKS);
			INSTANT_BREAK.add(Material.INFESTED_COBBLESTONE);
			INSTANT_BREAK.add(Material.INFESTED_CRACKED_STONE_BRICKS);
			INSTANT_BREAK.add(Material.INFESTED_MOSSY_STONE_BRICKS);
			INSTANT_BREAK.add(Material.INFESTED_STONE);
			INSTANT_BREAK.add(Material.INFESTED_STONE_BRICKS);
			INSTANT_BREAK.add(Material.TNT);
			INSTANT_BREAK.add(Material.SLIME_BLOCK);
			INSTANT_BREAK.add(Material.CARROTS);
			INSTANT_BREAK.add(Material.DEAD_BUSH);
			INSTANT_BREAK.add(Material.FERN);
			INSTANT_BREAK.add(Material.LARGE_FERN);
			INSTANT_BREAK.add(Material.CHORUS_FLOWER);
			INSTANT_BREAK.add(Material.SUNFLOWER);
			INSTANT_BREAK.add(Material.LILY_PAD);
			INSTANT_BREAK.add(Material.MELON_STEM);
			INSTANT_BREAK.add(Material.ATTACHED_MELON_STEM);
			INSTANT_BREAK.add(Material.BROWN_MUSHROOM);
			INSTANT_BREAK.add(Material.RED_MUSHROOM);
			INSTANT_BREAK.add(Material.NETHER_WART);
			INSTANT_BREAK.add(Material.POTATOES);
			INSTANT_BREAK.add(Material.PUMPKIN_STEM);
			INSTANT_BREAK.add(Material.ATTACHED_PUMPKIN_STEM);
			INSTANT_BREAK.add(Material.ACACIA_SAPLING);
			INSTANT_BREAK.add(Material.BIRCH_SAPLING);
			INSTANT_BREAK.add(Material.DARK_OAK_SAPLING);
			INSTANT_BREAK.add(Material.JUNGLE_SAPLING);
			INSTANT_BREAK.add(Material.OAK_SAPLING);
			INSTANT_BREAK.add(Material.SPRUCE_SAPLING);
			INSTANT_BREAK.add(Material.SUGAR_CANE);
			INSTANT_BREAK.add(Material.TALL_GRASS);
			INSTANT_BREAK.add(Material.TALL_SEAGRASS);
			INSTANT_BREAK.add(Material.WHEAT);
			// Start 1.14 objects
			if (VersionUtil.isOfVersion("v1_14") || VersionUtil.isOfVersion("v1_15")
					|| VersionUtil.isOfVersion("v1_16")) {
				INSTANT_BREAK.add(Material.BAMBOO_SAPLING);
				INSTANT_BREAK.add(Material.CORNFLOWER);
			}
			// End 1.14 objects
			// Start 1.15 objects
			if (VersionUtil.isOfVersion("v1_15") || VersionUtil.isOfVersion("v1_16")) {
				INSTANT_BREAK.add(Material.HONEY_BLOCK);
			}
			// End 1.15 objects
			// End instant break materials

			// Start food
			FOOD.add(Material.APPLE);
			FOOD.add(Material.BAKED_POTATO);
			FOOD.add(Material.BEETROOT);
			FOOD.add(Material.BEETROOT_SOUP);
			FOOD.add(Material.BREAD);
			FOOD.add(Material.CAKE);
			FOOD.add(Material.CARROT);
			FOOD.add(Material.CHORUS_FRUIT);
			FOOD.add(Material.COOKED_BEEF);
			FOOD.add(Material.COOKED_CHICKEN);
			FOOD.add(Material.COOKED_COD);
			FOOD.add(Material.COOKED_MUTTON);
			FOOD.add(Material.COOKED_PORKCHOP);
			FOOD.add(Material.COOKED_RABBIT);
			FOOD.add(Material.COOKED_SALMON);
			FOOD.add(Material.COOKIE);
			FOOD.add(Material.DRIED_KELP);
			FOOD.add(Material.ENCHANTED_GOLDEN_APPLE);
			FOOD.add(Material.GOLDEN_APPLE);
			FOOD.add(Material.GOLDEN_CARROT);
			FOOD.add(Material.MELON_SLICE);
			FOOD.add(Material.MUSHROOM_STEW);
			FOOD.add(Material.POISONOUS_POTATO);
			FOOD.add(Material.POTATO);
			FOOD.add(Material.PUFFERFISH);
			FOOD.add(Material.PUMPKIN_PIE);
			FOOD.add(Material.RABBIT_STEW);
			FOOD.add(Material.BEEF);
			FOOD.add(Material.CHICKEN);
			FOOD.add(Material.COD);
			FOOD.add(Material.MUTTON);
			FOOD.add(Material.PORKCHOP);
			FOOD.add(Material.RABBIT);
			FOOD.add(Material.SALMON);
			FOOD.add(Material.ROTTEN_FLESH);
			FOOD.add(Material.SPIDER_EYE);
			FOOD.add(Material.TROPICAL_FISH);
			// Start 1.14 objects
			if (VersionUtil.isOfVersion("v1_14") || VersionUtil.isOfVersion("v1_15")
					|| VersionUtil.isOfVersion("v1_16")) {
				FOOD.add(Material.SUSPICIOUS_STEW);
				FOOD.add(Material.SWEET_BERRIES);
			}
			// End 1.14 objects
			// Start 1.15 objects
			if (VersionUtil.isOfVersion("v1_15") || VersionUtil.isOfVersion("v1_16")) {
				FOOD.add(Material.HONEY_BOTTLE);
			}
			// End 1.15 objects
			// End food

			// Start combos
			COMBO.put(Material.SHEARS, Material.BLACK_WOOL);
			COMBO.put(Material.SHEARS, Material.BLUE_WOOL);
			COMBO.put(Material.SHEARS, Material.BROWN_WOOL);
			COMBO.put(Material.SHEARS, Material.CYAN_WOOL);
			COMBO.put(Material.SHEARS, Material.GRAY_WOOL);
			COMBO.put(Material.SHEARS, Material.GREEN_WOOL);
			COMBO.put(Material.SHEARS, Material.LIGHT_BLUE_WOOL);
			COMBO.put(Material.SHEARS, Material.LIGHT_GRAY_WOOL);
			COMBO.put(Material.SHEARS, Material.LIME_WOOL);
			COMBO.put(Material.SHEARS, Material.MAGENTA_WOOL);
			COMBO.put(Material.SHEARS, Material.MAGENTA_WOOL);
			COMBO.put(Material.SHEARS, Material.ORANGE_WOOL);
			COMBO.put(Material.SHEARS, Material.PINK_WOOL);
			COMBO.put(Material.SHEARS, Material.PURPLE_WOOL);
			COMBO.put(Material.SHEARS, Material.RED_WOOL);
			COMBO.put(Material.SHEARS, Material.WHITE_WOOL);
			COMBO.put(Material.SHEARS, Material.YELLOW_WOOL);

			COMBO.put(Material.IRON_SWORD, Material.COBWEB);
			COMBO.put(Material.DIAMOND_SWORD, Material.COBWEB);
			COMBO.put(Material.STONE_SWORD, Material.COBWEB);
			COMBO.put(Material.WOODEN_SWORD, Material.COBWEB);
			// End combos

			// Start climbable
			CLIMBABLE.add(Material.VINE);
			CLIMBABLE.add(Material.LADDER);
			CLIMBABLE.add(Material.WATER);
			// Start 1.14 objects
			if (VersionUtil.isOfVersion("v1_14") || VersionUtil.isOfVersion("v1_15")
					|| VersionUtil.isOfVersion("v1_16")) {
				CLIMBABLE.add(Material.SCAFFOLDING);
				CLIMBABLE.add(Material.SWEET_BERRY_BUSH);
			}
			// End 1.14 objects

			// Start 1.16 objects
			if (VersionUtil.isOfVersion("v1_16")) {
				CLIMBABLE.add(XMaterial.TWISTING_VINES.parseMaterial());
				CLIMBABLE.add(XMaterial.TWISTING_VINES_PLANT.parseMaterial());
				CLIMBABLE.add(XMaterial.WEEPING_VINES.parseMaterial());
				CLIMBABLE.add(XMaterial.WEEPING_VINES_PLANT.parseMaterial());
			}
			// End 1.16 objects
			// End climbable
		}
		// End other versions
	}
}
