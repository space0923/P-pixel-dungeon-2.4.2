package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.Random;

/**
 * CONFIDENT talent (Crew Chief T2):
 * Persists on the hero while the talent is active and powers the HUD ActionIndicator.
 * Click the button → wand-style targeting → attempt domination on selected enemy.
 * On success: applies DominatedBuff (cowers to nearest wall).
 * On fail: "Resisted!" floating text; turn still spent.
 */
public class ConfidentAbility extends Buff implements ActionIndicator.Action {

    { type = buffType.POSITIVE; }

    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)) {
            ActionIndicator.setAction(this);
            return true;
        }
        return false;
    }

    @Override
    public void detach() {
        ActionIndicator.clearAction(this);
        super.detach();
    }

    @Override
    public boolean act() {
        if (target instanceof Hero && ((Hero) target).hasTalent(Talent.CONFIDENT)) {
            ActionIndicator.setAction(this);
        } else {
            detach();
        }
        spend(TICK);
        return true;
    }

    @Override public int icon() { return BuffIndicator.NONE; }

    // ---- ActionIndicator.Action ----

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public int actionIcon() {
        return HeroIcon.NONE;
    }

    @Override
    public Visual primaryVisual() {
        Image icon = new HeroIcon(this);
        icon.hardlight(1f, 0.7f, 0f);
        return icon;
    }

    @Override
    public int indicatorColor() {
        return 0xFFBB44;
    }

    @Override
    public void doAction() {
        GameScene.selectCell(selector);
    }

    private final CellSelector.Listener selector = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            Hero hero = Dungeon.hero;
            int pts = hero.pointsInTalent(Talent.CONFIDENT);
            int range = pts; // L1 = 1 tile range, L2 = 2 tile range

            Char ch = Actor.findChar(cell);
            if (!(ch instanceof Mob)) {
                GLog.w(Messages.get(ConfidentAbility.class, "no_target"));
                return;
            }
            Mob mob = (Mob) ch;

            if (Dungeon.level.distance(hero.pos, mob.pos) > range) {
                GLog.w(Messages.get(ConfidentAbility.class, "out_of_range"));
                return;
            }

            if (mob.alignment != Char.Alignment.ENEMY
                    || !mob.isAlive()
                    || mob.buff(DominatedBuff.class) != null) {
                GLog.w(Messages.get(ConfidentAbility.class, "no_target"));
                return;
            }

            if (mob.properties().contains(Char.Property.BOSS)
                    || mob.properties().contains(Char.Property.MINIBOSS)) {
                GLog.w(Messages.get(ConfidentAbility.class, "resist_boss"));
                hero.spendAndNext(1f);
                return;
            }

            hero.spendAndNext(1f);

            float baseChance = (pts == 1) ? 0.10f : 0.15f;
            float healthFactor = 1f - ((float) mob.HP / mob.HT);
            float chance = Math.min(baseChance + healthFactor * 0.90f, 1f);

            if (Random.Float() < chance) {
                Buff.affect(mob, DominatedBuff.class);
                mob.sprite.showStatus(CharSprite.POSITIVE,
                        Messages.get(ConfidentAbility.class, "dominated"));
                GLog.p(Messages.get(ConfidentAbility.class, "success", mob.name()));
            } else {
                mob.sprite.showStatus(CharSprite.WARNING,
                        Messages.get(ConfidentAbility.class, "resisted"));
                GLog.w(Messages.get(ConfidentAbility.class, "fail", mob.name()));
            }
        }

        @Override
        public String prompt() {
            return Messages.get(ConfidentAbility.class, "prompt");
        }
    };
}
