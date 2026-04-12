/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SwarmSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Swarm extends Mob {

	{
		spriteClass = SwarmSprite.class;
		
		HP = HT = 180;
		defenseSkill = 5;

		EXP = 3;
		maxLvl = 9;
		
		flying = true;

		loot = new PotionOfHealing();
		lootChance = 0.1667f; //by default, see lootChance()
	}
	
	private static final float TIME_TO_ZAP	= 1f;
	
	@Override
	public int damageRoll() {
		return Char.combatRoll( 10, 14 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy)
				|| new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica( pos, enemy.pos, com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica( pos, enemy.pos, com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			return super.doAttack( enemy );
		} else {
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				((SwarmSprite)sprite).zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}
	
	protected void zap() {
		spend( TIME_TO_ZAP );
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			int dmg = Char.combatRoll( 10, 14 );
			dmg = Math.round(dmg * com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge.statModifier(this));
			enemy.damage( dmg, this );
		} else {
			enemy.sprite.showStatus( com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEUTRAL, enemy.defenseVerb() );
		}
	}
	
	public void onZapComplete() {
		zap();
		next();
	}

	@Override
	public float lootChance() {
		lootChance = 1f/6f;
		return super.lootChance() * (5f - Dungeon.LimitedDrops.SWARM_HP.count) / 5f;
	}
	
	@Override
	public Item createLoot(){
		Dungeon.LimitedDrops.SWARM_HP.count++;
		return super.createLoot();
	}
}
