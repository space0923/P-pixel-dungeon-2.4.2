package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * Applied to an enemy dominated via the CONFIDENT talent.
 *
 * Sets the mob's alignment to NEUTRAL and state to PASSIVE so it stops
 * attacking and holds its ground — acting as a "hostage" that can later
 * be converted to a Joker ally via the JOKER talent.
 *
 * Detaching (e.g. on mob death or JOKER conversion) restores the mob's
 * alignment to ENEMY if it was not already changed by another effect.
 */
public class DominatedBuff extends Buff {

    { type = buffType.POSITIVE; }

    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)) {
            if (target instanceof Mob) {
                Mob mob = (Mob) target;
                mob.alignment = Char.Alignment.NEUTRAL;
                mob.state = mob.PASSIVE;
            }
            target.paralysed++;
            return true;
        }
        return false;
    }

    @Override
    public boolean act() {
        spend(TICK);
        return true;
    }

    @Override
    public void detach() {
        if (target instanceof Mob) {
            Mob mob = (Mob) target;
            // Only restore enemy alignment if it hasn't been changed to ALLY
            // (e.g. by the JOKER talent applying JokerAllyBuff before detaching this)
            if (mob.alignment == Char.Alignment.NEUTRAL) {
                mob.alignment = Char.Alignment.ENEMY;
                mob.state = mob.WANDERING;
            }
        }
        target.paralysed--;
        super.detach();
    }

    @Override
    public int icon() {
        return BuffIndicator.NONE;
    }

    @Override
    public String toString() {
        return "Dominated";
    }

    @Override
    public String desc() {
        return "This enemy has been dominated and will not attack. It can be converted into a loyal Joker.";
    }
}
