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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/**
 * Buff assigned to select mobs at level creation to designate them as "guards".
 * 
 * Guard behavior:
 * 1. Patrols normally (SLEEPING/WANDERING) like any mob
 * 2. When alerted (spots hero or takes damage) -> switches to FLEEING
 * 3. Runs away from the player
 * 4. Once the guard breaks the hero's line of sight -> triggers the alarm
 * 5. After sounding alarm, becomes a normal fighting mob (HUNTING)
 * 
 * Player counterplay: Kill the guard before it escapes your FOV.
 */
public class GuardBuff extends Buff {

	private boolean fleeing = false;

	{
		actPriority = BUFF_PRIO;
	}

	@Override
	public boolean act() {
		if (target instanceof Mob && fleeing) {
			Mob mob = (Mob) target;

			// Check if the guard has broken the hero's line of sight
			boolean heroCanSeeGuard = Dungeon.level.heroFOV[mob.pos];

			if (!heroCanSeeGuard) {
				// Guard escaped! Sound the alarm!
				if (Dungeon.heistManager != null) {
					Dungeon.heistManager.triggerAlarm();
				}
				GLog.n(Messages.get(GuardBuff.class, "alarm_sounded"));

				// Guard becomes a normal fighting mob after calling alarm
				fleeing = false;
				mob.state = mob.HUNTING;
			}
		}
		spend(Actor.TICK);
		return true;
	}

	/**
	 * Called when the guard is alerted — spots the hero or takes damage.
	 * Only triggers during STEALTH phase (no point fleeing if alarm already went off).
	 */
	public void alert() {
		if (!fleeing && target instanceof Mob) {
			// Only flee if we're still in stealth mode
			if (Dungeon.heistManager != null
					&& Dungeon.heistManager.phase == HeistManager.Phase.STEALTH) {
				fleeing = true;
				Mob mob = (Mob) target;
				mob.state = mob.FLEEING;
				GLog.w(Messages.get(GuardBuff.class, "alert"));
			}
		}
	}

	public boolean isFleeing() {
		return fleeing;
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR; // Use armor icon to visually tag guards
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	// --- Save/Load ---

	private static final String FLEEING_KEY = "fleeing";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(FLEEING_KEY, fleeing);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		fleeing = bundle.getBoolean(FLEEING_KEY);
	}
}
