/*
 * Payday Pixel Dungeon
 * Copyright (C) 2024 space0923
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Buff attached to mobs spawned during ASSAULT phase.
 * Mobs start in HUNTING state. If they can't see the hero for
 * 8-12 consecutive turns, they de-escalate to WANDERING.
 * Seeing the hero resets the counter.
 */
public class AssaultAlert extends Buff {

	private int turnsWithoutSight = 0;
	private int maxTurns;

	{
		actPriority = BUFF_PRIO;
	}

	public AssaultAlert() {
		maxTurns = Random.IntRange(8, 12);
	}

	@Override
	public boolean act() {
		if (target instanceof Mob) {
			Mob mob = (Mob) target;

			boolean canSeeHero = mob.fieldOfView != null
					&& Dungeon.hero.pos < mob.fieldOfView.length
					&& mob.fieldOfView[Dungeon.hero.pos]
					&& Dungeon.hero.invisible <= 0;

			if (canSeeHero) {
				turnsWithoutSight = 0; // reset counter — mob sees the hero
			} else {
				turnsWithoutSight++;
				if (turnsWithoutSight >= maxTurns) {
					// De-escalate: mob gives up the search
					mob.state = mob.WANDERING;
					mob.clearEnemy();
					detach();
					return true;
				}
			}
		}
		spend(Actor.TICK);
		return true;
	}

	// --- Save/Load ---

	private static final String TURNS = "turns_without_sight";
	private static final String MAX = "max_turns";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsWithoutSight);
		bundle.put(MAX, maxTurns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsWithoutSight = bundle.getInt(TURNS);
		maxTurns = bundle.getInt(MAX);
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE; // invisible buff — players don't need to see it on mobs
	}
}
