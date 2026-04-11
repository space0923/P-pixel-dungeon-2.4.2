package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class KSP58 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.LMG;
		tier = 16;
		baseDamage = 80;
		ACC = 0.68f;
		accuracyFalloff = 0.06f;
		
		maxAmmo = 200;
		curAmmo = maxAmmo;
		maxReserveAmmo = 400;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 6;
		RPM = 909;

		DLY = 1.5f; // Heavy and clunky
		reloadDelay = 3.5f; // Long reload animation equivalent

		bones = false;
	}

}
