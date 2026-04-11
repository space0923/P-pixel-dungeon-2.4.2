package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DebugEnemy;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SewerPainter;

public class DebugLevel extends RegularLevel {

	{
		color1 = 0x5a5a5a;
		color2 = 0x6a6a6a;
	}

	@Override
	protected Painter painter() {
		return new SewerPainter().setWater(0, 0).setGrass(0, 0).setTraps(0, null, null);
	}

	@Override
	public String tilesTex() {
		return com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		setSize(32, 32);
		boolean[] pMap = new boolean[length()];
		
		java.util.Arrays.fill(map, Terrain.EMPTY);
		java.util.Arrays.fill(pMap, true);

		// Build a 16x16 walled box in the center
		int cx = width() / 2;
		int cy = height() / 2;

		for (int i=0; i<length(); i++) {
			int x = i % width();
			int y = i / width();

			if (x < cx - 8 || x > cx + 8 || y < cy - 8 || y > cy + 8) {
				map[i] = Terrain.WALL;
				pMap[i] = false;
			}
		}

		// Couple of internal walls to test
		map[cx + (cy-2)*width()] = Terrain.WALL;
		map[cx + (cy+2)*width()] = Terrain.WALL;

		// Couple of doors
		map[cx - 2 + cy*width()] = Terrain.DOOR;
		map[cx + 2 + cy*width()] = Terrain.DOOR;

		entrance = cx + cy * width();
		exit = cx + (cy-7) * width(); // So there's a way out? Or maybe no exit

		map[entrance] = Terrain.ENTRANCE;

		transitions.add(new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(this, entrance, com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(this, exit, com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_EXIT));

		rooms = new java.util.ArrayList<>(); // Prevent crash on saving level

		return true;
	}

	@Override
	protected void createMobs() {
		// Spawn 5 debug rats around the room
		int spawnAttempts = 0;
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			if (spawnAttempts >= 5) break;
			int mobCell = entrance + (i * 2); // Spread them out slightly
			if (mobCell > 0 && mobCell < length() && map[mobCell] == Terrain.EMPTY) {
				DebugEnemy rat = new DebugEnemy();
				rat.pos = mobCell;
				rat.state = rat.PASSIVE;
				mobs.add(rat);
				spawnAttempts++;
			}
		}

		// Spawn guns
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AMCAR4(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Compact5(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.M308(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.KSP58(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.R700(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Reinfeld880(), entrance);
		drop(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GL40(), entrance);
	}

	@Override
	protected void createItems() {
		// No random items loop needed, everything is hand-dropped in createMobs
	}

	@Override
	public int mobLimit() {
		return 0; // Don't spawn any extra natural enemies
	}
}
