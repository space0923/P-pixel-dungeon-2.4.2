package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;

public class DebugEnemy extends Mob {

	{
		spriteClass = RatSprite.class;
		
		HP = HT = 9999999;
		defenseSkill = 0; // Don't dodge, so we can test damage smoothly
		
		properties.add(Property.UNDEAD); // So it doesn't run away terrified
		
		state = PASSIVE; // Stationary dummy
	}

	@Override
	public int damageRoll() {
		return 0; // Deal no damage
	}

	@Override
	public int attackSkill(com.shatteredpixel.shatteredpixeldungeon.actors.Char target) {
		return 0;
	}
}
