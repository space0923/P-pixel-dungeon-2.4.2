package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class KSP58 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.LMG;
		tier = 10;
		baseDamage = 50;
		ACC = 0.8f;
		accuracyFalloff = 0.08f;
		
		maxAmmo = 200;
		curAmmo = maxAmmo;
		maxReserveAmmo = 400;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 6;
		
		DLY = 1.5f; // Heavy and clunky
		reloadDelay = 3.0f; // Long reload animation equivalent

		bones = false;
	}

}
