package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Rattlesnake extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.SNIPER;
		tier = 10;
		baseDamage = 120;
		ACC = 2.0f;
		accuracyFalloff = 0.0f; // Perfect accuracy
		
		maxAmmo = 10;
		curAmmo = maxAmmo;
		maxReserveAmmo = 30;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 1;
		
		DLY = 2.0f; // Slow to chamber rounds
		reloadDelay = 1.0f;

		bones = false;
	}

}
