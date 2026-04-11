package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class AMCAR4 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.ASSAULT_RIFLE;
		tier = 12;
		baseDamage = 45;
		ACC = 1.0f;
		accuracyFalloff = 0.04f;
		
		maxAmmo = 20;
		curAmmo = maxAmmo;
		maxReserveAmmo = 100;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 3;
		RPM = 600;
		
		DLY = 1.0f;
		reloadDelay = 2.0f;

		bones = false;
	}

}
