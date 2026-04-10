package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class HeistArmor extends ShieldBuff {

	private static final int BASE_REGEN_TURNS = 6;
	private static final String TURNS_SINCE_HIT = "turnsSinceHit";

	private int turnsSinceHit = 0;
	private Armor armor;

	public synchronized void setArmor(Armor armor) {
		this.armor = armor;
		if (target != null) {
			if (armor != null) {
				// Initialize shield to max on equip
				incShield(maxShield() - shielding());
			}
			target.needsShieldUpdate = true;
			BuffIndicator.refreshHero();
		}
	}

	public synchronized int maxShield() {
		if (armor != null && target instanceof Hero) {
			int base = (armor.tier * 10) + (armor.level() * 2);
			// TESTUDO L2: +10% max shield capacity
			if (((Hero)target).pointsInTalent(Talent.TESTUDO) == 2) {
				base = Math.round(base * 1.10f);
			}
			return Math.max(0, base);
		}
		return 0;
	}

	@Override
	public synchronized boolean act() {
		if (shielding() < maxShield()) {
			turnsSinceHit++;
			// TESTUDO L1/L2: reduce regen timer by 10% per point (6 → ~5.4 → ~4.8)
			int regenTurns = BASE_REGEN_TURNS;
			if (target instanceof Hero) {
				int testudoPts = ((Hero)target).pointsInTalent(Talent.TESTUDO);
				if (testudoPts > 0) {
					regenTurns = Math.round(BASE_REGEN_TURNS * (1f - 0.10f * testudoPts));
				}
			}
			if (turnsSinceHit >= regenTurns) {
				incShield(maxShield() - shielding());
			}
			BuffIndicator.refreshHero();
		}
		spend(TICK);
		return true;
	}

	@Override
	public int absorbDamage(int dmg) {
		turnsSinceHit = 0;
		BuffIndicator.refreshHero();
		
		if (shielding() <= 0) return dmg;

		if (shielding() >= dmg) {
			decShield(dmg);
			if (target != null && target.sprite != null) {
				com.watabou.noosa.audio.Sample.INSTANCE.play(com.watabou.utils.Random.element(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_ARMOR));
			}
			return 0;
		} else {
			decShield(shielding());
			
			if (target != null && target.sprite != null) {
				target.sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE, "Armor Broken!");
				com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.ARMOR_BREAK);
			}
			
			return 0; // Completely block all leftover damage (Armor Gating)
		}
	}

	@Override
	public int icon() {
		if (shielding() < maxShield()) {
			return BuffIndicator.VULNERABLE;
		}
		return BuffIndicator.ARMOR;
	}

	@Override
	public String iconTextDisplay() {
		if (shielding() < maxShield()) {
			int regenTurns = BASE_REGEN_TURNS;
			if (target instanceof Hero) {
				int testudoPts = ((Hero)target).pointsInTalent(Talent.TESTUDO);
				if (testudoPts > 0) {
					regenTurns = Math.round(BASE_REGEN_TURNS * (1f - 0.10f * testudoPts));
				}
			}
			return String.valueOf(Math.max(0, regenTurns - turnsSinceHit));
		}
		return String.valueOf(shielding());
	}

	@Override
	public String toString() {
		return "Armor Recovery";
	}

	@Override
	public String name() {
		if (shielding() == 0 && maxShield() > 0) {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this, "name_broken");
		}
		return super.name();
	}

	@Override
	public String desc() {
		if (shielding() == 0 && maxShield() > 0) {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this, "desc_broken");
		}
		return super.desc();
	}

	@Override
	public void detach() {
		// When shield hits 0: stay attached so we can regen (if armor is still equipped).
		// When armor is null (unequipped): actually detach.
		if (shielding() == 0 && armor != null && target instanceof Hero && armor.isEquipped((Hero)target)) {
			// Do nothing. Stay attached to regenerate.
			return;
		}
		target.needsShieldUpdate = true;
		super.detach();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS_SINCE_HIT, turnsSinceHit);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsSinceHit = bundle.getInt(TURNS_SINCE_HIT);
	}
}
