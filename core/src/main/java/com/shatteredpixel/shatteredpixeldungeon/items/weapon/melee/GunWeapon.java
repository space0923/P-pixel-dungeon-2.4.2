package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class GunWeapon extends Weapon {

	public static class Bullet extends Item {
		{
			image = ItemSpriteSheet.BULLET;
		}
	}
	public static final Bullet BULLET = new Bullet();

	public static final String AC_SHOOT = "SHOOT"; // "Shoot" action name
	public static final String AC_RELOAD = "RELOAD"; // "Reload" action name

	public int maxAmmo = 10;
	public int curAmmo = maxAmmo;
	
	public int maxReserveAmmo = 50;
	public int reserveAmmo = maxReserveAmmo;
	public float reloadDelay = 1.0f;
	public int maxRange = 6;
	public int tier;
	public int baseDamage = 0;
	public boolean silenced = false;

	@Override
	public int min(int lvl) {
		if (baseDamage > 0) {
			return baseDamage + (lvl * (tier + 1));
		}
		return tier + lvl;
	}

	@Override
	public int max(int lvl) {
		if (baseDamage > 0) {
			return baseDamage + (lvl * (tier + 1));
		}
		return 5 * (tier + 1) + lvl * (tier + 1);
	}

	@Override
	public int STRReq(int lvl) {
		return STRReq(tier, lvl);
	}

	@Override
	public int damageRoll(Char owner) {
		int fixedBaseDamage;
		if (baseDamage > 0) {
			fixedBaseDamage = baseDamage + (buffedLvl() * (tier + 1));
		} else {
			fixedBaseDamage = (min() + max()) / 2; 
		}
		
		int damage = augment.damageFactor(fixedBaseDamage);

		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += exStr; 
			}
		}
		return damage;
	}

	@Override
	public String info() {
		String info = desc();

		if (levelKnown) {
			// Using MeleeWeapon.class here to reuse existing translation strings
			info += "\n\n" + Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.class, "stats_known", tier, augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
			if (STRReq() > Dungeon.hero.STR()) {
				info += " " + Messages.get(Weapon.class, "too_heavy");
			} else if (Dungeon.hero.STR() > STRReq()){
				info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
			}
		} else {
			info += "\n\n" + Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.class, "stats_unknown", tier, min(0), max(0), STRReq(0));
			if (STRReq(0) > Dungeon.hero.STR()) {
				info += " " + Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.class, "probably_too_heavy");
			}
		}
		return info;
	}

	{
		defaultAction = AC_SHOOT;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (curAmmo > 0) {
			actions.add(AC_SHOOT);
		}
		if (curAmmo < maxAmmo && reserveAmmo > 0) {
			actions.add(AC_RELOAD);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_SHOOT)) {
			if (curAmmo <= 0) {
				return;
			}
			curUser = hero;
			curItem = this;
			GameScene.selectCell(shooter);
		} else if (action.equals(AC_RELOAD)) {
			if (curAmmo >= maxAmmo || reserveAmmo <= 0) {
				return;
			}
			int amountToReload = Math.min(maxAmmo - curAmmo, reserveAmmo);
			curAmmo += amountToReload;
			reserveAmmo -= amountToReload;
			updateQuickslot();
			
			hero.spendAndNext(reloadDelay);
			hero.sprite.operate(hero.pos); // standard animation for using an item
			Sample.INSTANCE.play(Assets.Sounds.STEP); // placeholder reload sound
			hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(GunWeapon.class, "reloaded"));
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return new Ballistica(user.pos, dst, Ballistica.PROJECTILE).collisionPos;
	}

	protected CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null && curAmmo > 0) {
				if (Dungeon.level.distance(curUser.pos, target) > maxRange) {
					curUser.sprite.showStatus(CharSprite.NEUTRAL, "Out of range");
					return;
				}
				shoot(curUser, target);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(GunWeapon.class, "prompt");
		}
	};

	public void shoot(final Hero user, int dst) {
		final int cell = targetingPos(user, dst);
		curAmmo--;
		updateQuickslot();

		user.busy();

		if (!silenced) {
			Sample.INSTANCE.play(hitSound, 1, hitSoundPitch); // BANG!
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				// Alert enemies in a radius of roughly 15 tiles
				if (Dungeon.level.distance(user.pos, mob.pos) <= 15) {
					mob.beckon(user.pos);
				}
			}
		}

		user.sprite.zap(cell);
		com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite sprite = 
			(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)user.sprite.parent.recycle(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class);
		
		sprite.reset( user.pos,
				cell,
				BULLET,
				new Callback() {
					@Override
					public void call() {
						Char enemy = Actor.findChar(cell);
						if (enemy != null && enemy != user) {
							// Hit the enemy
							int dmg = damageRoll(user);
							enemy.damage(dmg, GunWeapon.this);
						}
						user.spendAndNext(delayFactor(user)); // use weapon speed delay
					}
				});
	}

	private static final String CUR_AMMO = "cur_ammo";
	private static final String RES_AMMO = "res_ammo";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CUR_AMMO, curAmmo);
		bundle.put(RES_AMMO, reserveAmmo);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		curAmmo = bundle.getInt(CUR_AMMO);
		reserveAmmo = bundle.getInt(RES_AMMO);
	}

	@Override
	public String status() {
		if (levelKnown) {
			return curAmmo + "/" + maxAmmo;
		} else {
			return null;
		}
	}
}
