package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Bernetti9 extends GunWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD;
		hitSound = Assets.Sounds.HIT_Chimano88; // Reusing Chimano88 sound for Bernetti
		hitSoundPitch = 1f;

		tier = 10;
		baseDamage = 53;
		maxRange = 3;
		maxAmmo = 14;
		curAmmo = maxAmmo;
		maxReserveAmmo = 154;
		reserveAmmo = maxReserveAmmo;
		RCH = 1;
		DLY = 0.5f;

		silenced = false; // By default without a silencer? Using chimano sound which says "with
							// suppressor"

		bones = false;
	}

}
