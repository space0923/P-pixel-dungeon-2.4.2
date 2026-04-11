package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class M308 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.MARKSMAN_RIFLE;
		tier = 14;
		baseDamage = 160;
		ACC = 1.4f;
		accuracyFalloff = 0.1f;
		
		maxAmmo = 10;
		curAmmo = maxAmmo;
		maxReserveAmmo = 70;
		reserveAmmo = maxReserveAmmo;
		
		burstCount = 1;
		
		DLY = 1.0f;
		reloadDelay = 2.0f;

		bones = false;
	}

}
