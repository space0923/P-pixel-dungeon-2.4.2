package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DominatedBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class CableTie extends Item {

	public static final String AC_TIE = "TIE";

	{
		image = ItemSpriteSheet.CABLETIE; // Replaced SEED_EARTHROOT placeholder
		stackable = true;
		defaultAction = AC_TIE;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_TIE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_TIE)) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell(tier);
		}
	}

	protected static CellSelector.Listener tier = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null) {
				Char ch = Actor.findChar(target);
				Hero hero = curUser;
				CableTie tie = (CableTie) curItem;

				if (ch == null || ch == hero) {
					GLog.w(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "no_target"));
					return;
				}

				if (Dungeon.level.distance(hero.pos, ch.pos) > 1) {
					GLog.w(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "out_of_range"));
					return;
				}

				if (!(ch instanceof Mob)) {
					return;
				}
				Mob mob = (Mob) ch;

				if (mob.alignment != Char.Alignment.ENEMY || !mob.isAlive() || mob.buff(DominatedBuff.class) != null) {
					GLog.w(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "no_target"));
					return;
				}

				if (mob.properties().contains(Char.Property.BOSS) || mob.properties().contains(Char.Property.MINIBOSS)) {
					GLog.w(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "resist_boss"));
					hero.spendAndNext(1f);
					return;
				}

				hero.spendAndNext(1f);

				float baseChance = 0.10f;
				float healthFactor = 1f - ((float) mob.HP / mob.HT);
				float chance = Math.min(baseChance + healthFactor * 0.90f, 1f);

				if (com.watabou.utils.Random.Float() < chance) {
					Buff.affect(mob, DominatedBuff.class);
					mob.sprite.showStatus(CharSprite.POSITIVE, Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "dominated"));
					GLog.p(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "success", mob.name()));
				} else {
					mob.sprite.showStatus(CharSprite.WARNING, Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "resisted"));
					GLog.w(Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ConfidentAbility.class, "fail", mob.name()));
				}
				
				tie.consume();

				// attempting to target the cell
				QuickSlotButton.target(mob);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(CableTie.class, "prompt");
		}
	};

	public void consume() {
		if (--quantity <= 0) {
			detach(curUser.belongings.backpack);
		} else {
			updateQuickslot();
		}
	}
}
