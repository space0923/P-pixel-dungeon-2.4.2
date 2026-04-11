package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Reinfeld880 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD; // Placeholder
		hitSound = Assets.Sounds.HIT_Chimano88; // Placeholder
		hitSoundPitch = 1f;

		gunType = GunType.SHOTGUN;
		tier = 14;
		baseDamage = 90;
		ACC = 1.0f;
		accuracyFalloff = 0.10f; // Severe accuracy loss at distance

		maxAmmo = 8;
		curAmmo = maxAmmo;
		maxReserveAmmo = 40;
		reserveAmmo = maxReserveAmmo;

		burstCount = 1;
		pellets = 6;

		DLY = 1.2f;
		reloadType = ReloadType.OAT;
		reloadDelay = 1.0f;

		bones = false;
	}

}
