package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;

public class GL40 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.SPECIAL;
		tier = 16;
		baseDamage = 0; // Handled by bomb logic natively
		ACC = 1.0f;
		accuracyFalloff = 0.0f;
		
		maxAmmo = 1;
		curAmmo = maxAmmo;
		maxReserveAmmo = 6;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 1;
		pellets = 1;
		
		DLY = 1.0f;
		reloadDelay = 2.0f;

		bones = false;
	}

	@Override
	public void shoot(final Hero user, int dst) {
		final int cell = targetingPos(user, dst);
		curAmmo--;
		updateQuickslot();

		user.busy();

		if (!silenced) {
			initSounds();
			if (fireSounds != null && fireSounds.length > 0) {
				String sound = com.watabou.utils.Random.element(fireSounds);
				com.watabou.noosa.audio.Sample.INSTANCE.play(sound, 1, hitSoundPitch);
			} else if (hitSound != null) {
				com.watabou.noosa.audio.Sample.INSTANCE.play(hitSound, 1, hitSoundPitch);
			}
		}

		user.sprite.zap(cell);
		com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite sprite = 
			(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)user.sprite.parent.recycle(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class);
		
		sprite.reset(user.pos, cell, BULLET, new Callback() {
			@Override
			public void call() {
				// Spawn explicit explosive payload
				new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.ConjuredBomb().explode(cell);
				user.spendAndNext(delayFactor(user)); // use weapon speed delay
			}
		});
	}

}
