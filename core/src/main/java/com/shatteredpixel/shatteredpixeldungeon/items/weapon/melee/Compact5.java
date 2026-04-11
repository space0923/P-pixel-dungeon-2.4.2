package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Compact5 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.SMG;
		tier = 12;
		baseDamage = 44;
		ACC = 1.0f;
		accuracyFalloff = 0.08f;
		
		maxAmmo = 30;
		curAmmo = maxAmmo;
		maxReserveAmmo = 120;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 3;
		RPM = 750;
		
		DLY = 0.75f;
		reloadDelay = 1.5f;

		bones = false;
	}

}
