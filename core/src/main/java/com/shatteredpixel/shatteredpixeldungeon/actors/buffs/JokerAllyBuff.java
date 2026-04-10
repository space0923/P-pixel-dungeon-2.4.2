package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * Applied when the player converts a Dominated enemy via the JOKER talent.
 *
 * Extends AllyBuff so the mob fights alongside the hero.
 * At JOKER level 2, also applies JokerEmpower for +30% melee damage.
 */
public class JokerAllyBuff extends AllyBuff {

    { type = buffType.POSITIVE; }

    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)) {
            // Remove dominated status now that mob is a full ally
            DominatedBuff dominated = target.buff(DominatedBuff.class);
            if (dominated != null) dominated.detach();

            // L2: grant permanent +30% damage empower
            if (Dungeon.hero != null && Dungeon.hero.pointsInTalent(Talent.JOKER) == 2) {
                Buff.affect(target, JokerEmpower.class);
            }
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
    public int icon() {
        return BuffIndicator.NONE;
    }

    @Override
    public String toString() {
        return "Joker";
    }

    @Override
    public String desc() {
        return "This enemy has been converted into a loyal Joker and will fight for you.";
    }

    /**
     * Flag buff — presence grants +30% melee damage (checked in Char.attack()).
     * Applied automatically by JokerAllyBuff at JOKER talent level 2.
     */
    public static class JokerEmpower extends Buff {

        { type = buffType.POSITIVE; }

        @Override
        public boolean act() {
            spend(TICK);
            return true;
        }

        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }
    }
}
