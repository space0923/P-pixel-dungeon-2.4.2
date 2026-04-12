package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

public class WndOffshoreUpgrades extends Window {

	private static final int WIDTH = 140;
	private static final int BTN_HEIGHT = 20;
	private static final int GAP = 4;

	public WndOffshoreUpgrades() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock("Offshore Network", 9);
		title.hardlight(TITLE_COLOR);
		add(title);

		ColorBlock sep1 = new ColorBlock(1, 1, 0xFF000000);
		add(sep1);

		RenderedTextBlock balance = PixelScene.renderTextBlock("Balance: " + SPDSettings.offshoreAccount() + "G", 8);
		add(balance);

		ColorBlock sep2 = new ColorBlock(1, 1, 0xFF000000);
		add(sep2);

		// Upgrades
		// 1. Starting Level Boost (+1 Starting level)
		RenderedTextBlock upg1Desc = PixelScene.renderTextBlock("Specialist Training (+1 Starting Level)\nCost: 1000G", 6);
		upg1Desc.maxWidth(WIDTH);
		add(upg1Desc);
		RedButton btnUpg1 = new RedButton("Purchase") {
			@Override
			protected void onClick() {
				if (!SPDSettings.unlockedStartingLevel() && SPDSettings.offshoreAccount() >= 1000) {
					SPDSettings.offshoreAccount(SPDSettings.offshoreAccount() - 1000);
					SPDSettings.unlockedStartingLevel(true);
					ShatteredPixelDungeon.scene().add(new WndOffshoreUpgrades());
					hide();
				}
			}
		};
		if (SPDSettings.unlockedStartingLevel()) {
			btnUpg1.text("Purchased");
			btnUpg1.enable(false);
		} else if (SPDSettings.offshoreAccount() < 1000) {
			btnUpg1.enable(false);
		}
		add(btnUpg1);

		// 2. Increased Gold Drops (+5% Gold)
		RenderedTextBlock upg2Desc = PixelScene.renderTextBlock("Insider Trading (+5% Gold Gain)\nCost: 2500G", 6);
		upg2Desc.maxWidth(WIDTH);
		add(upg2Desc);
		RedButton btnUpg2 = new RedButton("Purchase") {
			@Override
			protected void onClick() {
				if (SPDSettings.unlockedStartingLevel() && !SPDSettings.unlockedGoldGain() && SPDSettings.offshoreAccount() >= 2500) {
					SPDSettings.offshoreAccount(SPDSettings.offshoreAccount() - 2500);
					SPDSettings.unlockedGoldGain(true);
					ShatteredPixelDungeon.scene().add(new WndOffshoreUpgrades());
					hide();
				}
			}
		};
		if (SPDSettings.unlockedGoldGain()) {
			btnUpg2.text("Purchased");
			btnUpg2.enable(false);
		} else if (!SPDSettings.unlockedStartingLevel()) {
			btnUpg2.text("Locked");
			btnUpg2.enable(false);
			upg2Desc.hardlight(0x888888); // Grey out text if locked
		} else if (SPDSettings.offshoreAccount() < 2500) {
			btnUpg2.enable(false);
		}
		add(btnUpg2);

		// Layout
		float pos = GAP;
		title.setPos((WIDTH - title.width()) / 2f, pos);
		pos += title.height() + GAP;
		
		sep1.size(WIDTH, 1);
		sep1.y = pos;
		pos += GAP;

		balance.setPos((WIDTH - balance.width()) / 2f, pos);
		pos += balance.height() + GAP;

		sep2.size(WIDTH, 1);
		sep2.y = pos;
		pos += GAP;

		upg1Desc.setPos(0, pos);
		pos += upg1Desc.height() + GAP;
		btnUpg1.setRect(0, pos, WIDTH, BTN_HEIGHT);
		pos = btnUpg1.bottom() + GAP;

		upg2Desc.setPos(0, pos);
		pos += upg2Desc.height() + GAP;
		btnUpg2.setRect(0, pos, WIDTH, BTN_HEIGHT);
		pos = btnUpg2.bottom() + GAP;

		resize(WIDTH, (int)pos);
	}
}
