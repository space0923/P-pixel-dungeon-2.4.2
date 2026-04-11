package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class R700 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.SNIPER;
		tier = 16;
		baseDamage = 246;
		ACC = 1.7f;
		accuracyFalloff = 0.1f; // Perfect accuracy
		
		maxAmmo = 10;
		curAmmo = maxAmmo;
		maxReserveAmmo = 40;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 1;
		
		DLY = 1.5f; // Slow to chamber rounds
		reloadDelay = 2.0f;

		bones = false;
	}

}
