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

public abstract class GunWeapon extends MeleeWeapon {

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
	public enum GunType {
		PISTOL, ASSAULT_RIFLE, MARKSMAN_RIFLE, SMG, SHOTGUN, LMG, SNIPER, SPECIAL
	}
	public GunType gunType = GunType.PISTOL;

	public int reserveAmmo = 0;
	public float reloadDelay = 1.0f;

	public enum ReloadType { MAG, OAT }
	public ReloadType reloadType = ReloadType.MAG;

	public int burstCount = 1; // 1 for semi, >1 for burst/auto
	public int RPM = 400; // Rounds per minute for burst visually
	public int pellets = 1;
	
	public float accuracyFalloff = 0.05f;
	public boolean silenced = false;

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float acc = super.accuracyFactor(owner, target);
		if (target != null) {
			int distance = Dungeon.level.distance(owner.pos, target.pos);
			acc -= (distance * accuracyFalloff);
		}
		return Math.max(0.1f, acc);
	}

	@Override
	public String info() {
		String statsPrefix = "Type: [" + gunType.name() + "]\n" +
				"Accuracy: " + (int)(ACC * 100) + "%\n" +
				"Accuracy Falloff: " + (int)(accuracyFalloff * 100) + "% per tile\n" +
				"Reload Time: " + reloadDelay + " turns\n" +
				"Fire Delay: " + DLY + " turns\n";

		if (burstCount > 1) {
			statsPrefix += "Burst Size: " + burstCount + " bullets\n";
		}
		if (pellets > 1) {
			statsPrefix += "Pellet Count: " + pellets + " per shot\n";
		}
		statsPrefix += "\n";

		String info = statsPrefix + desc();

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
	
	public GunWeapon() {
		super();
		if (com.badlogic.gdx.Gdx.files != null) {
			com.watabou.noosa.Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					initSounds();
				}
			});
		}
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
			int amountToReload;
			if (reloadType == ReloadType.OAT) {
				amountToReload = 1;
			} else {
				amountToReload = Math.min(maxAmmo - curAmmo, reserveAmmo);
			}
			curAmmo += amountToReload;
			reserveAmmo -= amountToReload;
			updateQuickslot();
			
			if (reloadType == ReloadType.OAT) {
				if (curAmmo < maxAmmo && reserveAmmo > 0) {
					hero.curAction = new com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction.Reload(this);
				}
			}
			
			hero.spendAndNext(reloadDelay);
			
			hero.sprite.isMoving = true;
			hero.sprite.operate(hero.pos, new com.watabou.utils.Callback() {
				@Override
				public void call() {
					synchronized(hero.sprite) {
						hero.sprite.isMoving = false;
						hero.sprite.notifyAll(); // notify the logic thread waiting on the sprite
					}
				}
			});
			
			initSounds(); // Ensure reload sounds are loaded
			boolean playedSound = false;
			if (reloadSounds != null && reloadSounds.length > 0) {
				int numReload1 = reloadSounds.length;
				if (reloadSounds[numReload1 - 1].contains("reload_2")) {
					numReload1--;
				}
				
				if (numReload1 > 0) {
					int randIdx = com.watabou.utils.Random.Int(numReload1);
					Sample.INSTANCE.play(reloadSounds[randIdx]);
					playedSound = true;
				}
				
				if (reloadType == ReloadType.MAG && numReload1 < reloadSounds.length) {
					Sample.INSTANCE.playDelayed(reloadSounds[reloadSounds.length - 1], Math.max(0.1f, reloadDelay * 0.5f));
				}
			}

			if (!playedSound) {
				Sample.INSTANCE.play(Assets.Sounds.STEP); // placeholder reload sound
			}

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
				shoot(curUser, target);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(GunWeapon.class, "prompt");
		}
	};

	protected String[] fireSounds = null;
	protected String[] reloadSounds = null;

	protected void initSounds() {
		if (fireSounds != null) return;
		java.util.ArrayList<String> list = new java.util.ArrayList<>();
		String folder = "sounds/" + getClass().getSimpleName().toLowerCase(java.util.Locale.ENGLISH) + "/";
		int i = 1;
		while (true) {
			String path = folder + "fire_" + i + ".mp3";
			if (com.badlogic.gdx.Gdx.files.internal(path).exists()) {
				list.add(path);
				i++;
			} else {
				break;
			}
		}
		if (list.isEmpty()) {
			fireSounds = new String[0];
		} else {
			fireSounds = list.toArray(new String[0]);
			com.watabou.noosa.audio.Sample.INSTANCE.load(fireSounds);
		}

		java.util.ArrayList<String> reload1List = new java.util.ArrayList<>();
		int j = 1;
		while (true) {
			String path = folder + "reload_1_" + j + ".mp3";
			if (com.badlogic.gdx.Gdx.files.internal(path).exists()) {
				reload1List.add(path);
				j++;
			} else {
				break;
			}
		}
		if (com.badlogic.gdx.Gdx.files.internal(folder + "reload_1.mp3").exists()) {
			reload1List.add(folder + "reload_1.mp3");
		}

		// Store reload sounds: index 0 to length-2 are reload_1, last index is reload_2
		int r2Offset = 0;
		if (com.badlogic.gdx.Gdx.files.internal(folder + "reload_2.mp3").exists()) {
			r2Offset = 1;
		}
		
		reloadSounds = new String[reload1List.size() + r2Offset];
		for (int k = 0; k < reload1List.size(); k++) {
			reloadSounds[k] = reload1List.get(k);
			com.watabou.noosa.audio.Sample.INSTANCE.load(reloadSounds[k]);
		}
		
		if (r2Offset == 1) {
			reloadSounds[reloadSounds.length - 1] = folder + "reload_2.mp3";
			com.watabou.noosa.audio.Sample.INSTANCE.load(reloadSounds[reloadSounds.length - 1]);
		}
	}

	public void shoot(final Hero user, int dst) {
		final int cell = targetingPos(user, dst);
		final int shotsToFire = Math.max(1, Math.min(burstCount, curAmmo));
		curAmmo -= shotsToFire;
		updateQuickslot();

		user.busy();

		if (!silenced) {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (Dungeon.level.distance(user.pos, mob.pos) <= 15) {
					mob.beckon(user.pos);
				}
			}
		}

		user.sprite.zap(cell);
		java.util.HashSet<Char> hitEnemies = new java.util.HashSet<>();
		
		fireSpreadOrBurst(user, cell, shotsToFire, shotsToFire, hitEnemies);
	}

	private void fireSpreadOrBurst(final Hero user, final int cell, final int remainingBurst, final int totalBurst, final java.util.HashSet<Char> hitEnemies) {
		if (remainingBurst <= 0) {
			return;
		}

		if (!silenced) {
			initSounds();
			if (fireSounds != null && fireSounds.length > 0) {
				String sound = com.watabou.utils.Random.element(fireSounds);
				Sample.INSTANCE.play(sound, 1, hitSoundPitch);
			} else if (hitSound != null) {
				Sample.INSTANCE.play(hitSound, 1, hitSoundPitch);
			}
		}

		if (pellets > 1) {
			final int[] callbacksRemaining = new int[]{pellets};
			for (int p = 0; p < pellets; p++) {
				int targetCell = cell; 
				if (p > 0) {
					int[] neighbours = com.watabou.utils.PathFinder.NEIGHBOURS8;
					targetCell = cell + neighbours[com.watabou.utils.Random.Int(neighbours.length)];
				}
				
				int dist = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.distance(user.pos, targetCell);
				float finalAccMod = Math.max(0.1f, ACC - (dist * accuracyFalloff));
				boolean aimTrue = com.watabou.utils.Random.Float() <= finalAccMod;
				
				if (!aimTrue) {
					int[] neighbours = com.watabou.utils.PathFinder.NEIGHBOURS8;
					targetCell = targetCell + neighbours[com.watabou.utils.Random.Int(neighbours.length)];
				}

				com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica trace = 
					new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(user.pos, targetCell, com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE);
				final int finalDest = trace.collisionPos;

				if (finalDest < 0 || finalDest >= com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.length()) {
					callbacksRemaining[0]--;
					if (callbacksRemaining[0] == 0 && remainingBurst == 1) {
						user.spendAndNext(delayFactor(user));
					}
					continue;
				}

				com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite sprite = 
					(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)user.sprite.parent.recycle(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class);

				sprite.reset( user.pos, finalDest, BULLET, new Callback() {
					@Override
					public void call() {
						com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon prevThrown = user.belongings.thrownWeapon;
						user.belongings.thrownWeapon = GunWeapon.this;

						Char enemy = Actor.findChar(finalDest);
						if (enemy != null && enemy != user && !hitEnemies.contains(enemy)) {
							if (Char.hit(user, enemy, false)) {
								int dmg = damageRoll(user);
								enemy.damage(dmg, GunWeapon.this);
								hitEnemies.add(enemy);
							} else {
								enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
							}
						}

						if (remainingBurst == totalBurst) {
							com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.destroy(finalDest);
						}

						user.belongings.thrownWeapon = prevThrown;
						
						callbacksRemaining[0]--;
						if (callbacksRemaining[0] == 0 && remainingBurst == 1) {
							user.spendAndNext(delayFactor(user));
						}
					}
				});
			}

		} else {
			int dist = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.distance(user.pos, cell);
			float finalAccMod = Math.max(0.1f, ACC - (dist * accuracyFalloff));
			boolean aimTrue = com.watabou.utils.Random.Float() <= finalAccMod;
			
			int targetCell = cell;
			if (!aimTrue) {
				int[] neighbours = com.watabou.utils.PathFinder.NEIGHBOURS8;
				targetCell = targetCell + neighbours[com.watabou.utils.Random.Int(neighbours.length)];
			}

			com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica trace = 
				new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(user.pos, targetCell, com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE);
			final int finalDest = trace.collisionPos;

			if (finalDest < 0 || finalDest >= com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.length()) {
				if (remainingBurst == 1) user.spendAndNext(delayFactor(user));
				return;
			}

			com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite sprite = 
				(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)user.sprite.parent.recycle(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class);

			sprite.reset( user.pos, finalDest, BULLET, new Callback() {
				@Override
				public void call() {
					com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon prevThrown = user.belongings.thrownWeapon;
					user.belongings.thrownWeapon = GunWeapon.this;
					
					Char enemy = Actor.findChar(finalDest);
					if (enemy != null && enemy != user) {
						if (Char.hit(user, enemy, false)) {
							int dmg = damageRoll(user);
							enemy.damage(dmg, GunWeapon.this);
						} else {
							enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
						}
					}
					
					if (remainingBurst == totalBurst) {
						com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.destroy(finalDest);
					}

					user.belongings.thrownWeapon = prevThrown;
					
					if (remainingBurst == 1) {
						user.spendAndNext(delayFactor(user));
					}
				}
			});
		}

		if (remainingBurst - 1 > 0) {
			float delaySeconds = 60.0f / RPM;
			user.sprite.parent.add(new com.watabou.noosa.tweeners.Delayer(delaySeconds) {
				@Override
				protected void onComplete() {
					hitEnemies.clear();     
					fireSpreadOrBurst(user, cell, remainingBurst - 1, totalBurst, hitEnemies);
				}
			});
		}
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
