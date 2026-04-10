package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GunWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class AmmoBox extends Item {

	{
		image = ItemSpriteSheet.AMMO_BOX; // Replaced RATION placeholder
		stackable = false; // They don't go in inventory, consumed on pickup
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		return new ArrayList<>(); // Cannot be used manually from inventory, auto-pickup only
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		
		boolean restoredAny = false;
		int totalGained = 0;

		// Check primary weapon
		if (hero.belongings.weapon() instanceof GunWeapon) {
			totalGained += restoreAmmo((GunWeapon) hero.belongings.weapon());
			restoredAny = true;
		}

		// Check secondary weapon
		if (hero.belongings.secondWep() instanceof GunWeapon) {
			totalGained += restoreAmmo((GunWeapon) hero.belongings.secondWep());
			restoredAny = true;
		}

		// Check quickslots
		for (int i = 0; i < QuickSlot.SIZE; i++) {
			Item qItem = Dungeon.quickslot.getItem(i);
			if (qItem instanceof GunWeapon && qItem != hero.belongings.weapon() && qItem != hero.belongings.secondWep()) {
				totalGained += restoreAmmo((GunWeapon) qItem);
				restoredAny = true;
			}
		}

		if (restoredAny && totalGained > 0) {
			GameScene.pickUp(this, pos);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "+" + totalGained, com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText.AMMO); 
			hero.spendAndNext(TIME_TO_PICK_UP);
			
			// Play generic interaction sound
			Sample.INSTANCE.play(Assets.Sounds.STEP);
			return true;
		}

		// If no guns to restore ammo for, simply don't pick it up
		if (!restoredAny) {
			// If we want it to sit on ground when full, we return false here.
			// But returning false means they step on it repeatedly with no effect.
			// Just leave it on ground.
			return false;
		}
		
		// If guns existed but all were fully stocked
		return false;
	}

	private int restoreAmmo(GunWeapon gun) {
		int missing = gun.maxReserveAmmo - gun.reserveAmmo;
		if (missing > 0) {
			// Restore 10% of the max reserve ammo per box
			int amountToRestore = Math.max(1, (int)(gun.maxReserveAmmo * 0.10f));
			amountToRestore = Math.min(amountToRestore, missing);
			gun.reserveAmmo += amountToRestore;
			gun.updateQuickslot();
			return amountToRestore;
		}
		return 0;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
