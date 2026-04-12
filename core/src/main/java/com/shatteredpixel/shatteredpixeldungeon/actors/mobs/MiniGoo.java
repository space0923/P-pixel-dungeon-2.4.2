package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeSprite;

public class MiniGoo extends Mob {
	
	{
		spriteClass = SlimeSprite.class;
		
		HP = HT = 60;
		defenseSkill = 10;
		baseSpeed = 2f;
		
		EXP = 0; // No EXP farming
		maxLvl = 9;
		
		lootChance = 0f; // No loot
	}
	
	@Override
	public int damageRoll() {
		return Char.combatRoll( 4, 6 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 12;
	}
}
