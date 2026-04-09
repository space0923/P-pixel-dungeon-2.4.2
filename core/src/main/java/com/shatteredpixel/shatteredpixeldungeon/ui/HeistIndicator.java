/*
 * Payday Pixel Dungeon
 * Copyright (C) 2024 space0923
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeistManager;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;

/**
 * HUD indicator showing the current heist phase.
 * Displays in the top-right area of the screen.
 *
 * CASING:       Gray text "STEALTH"
 * CONTROL:      Flashing yellow "ALARM!"
 * ANTICIPATION: Pulsing yellow "GET READY"
 * ASSAULT/FADE: Pulsing red "ASSAULT #N" (fade is a hidden sub-state)
 */
public class HeistIndicator extends Tag {

	private BitmapText phaseText;
	private HeistManager.Phase lastPhase = null;
	private float flashTimer = 0f;

	public HeistIndicator() {
		super(0x222222);
		setSize(80, 16);
		visible = true;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		phaseText = new BitmapText(PixelScene.pixelFont);
		phaseText.alpha(0.9f);
		add(phaseText);
	}

	@Override
	protected void layout() {
		super.layout();

		if (phaseText != null) {
			phaseText.x = x + (width - phaseText.width()) / 2f;
			phaseText.y = y + (height - phaseText.baseLine()) / 2f;
			PixelScene.align(phaseText);
		}
	}

	@Override
	public void update() {
		super.update();

		if (Dungeon.heistManager == null) {
			visible = false;
			return;
		}

		visible = true;
		HeistManager.Phase phase = Dungeon.heistManager.phase;
		flashTimer += Game.elapsed;

		// Only rebuild text when phase changes
		if (phase != lastPhase) {
			lastPhase = phase;
			updatePhaseDisplay(phase);
		}

		// Pulsing/flashing effects
		switch (phase) {
			case CONTROL:
				// Flash between visible and invisible
				float flashAlpha = (float) Math.abs(Math.sin(flashTimer * 4f));
				phaseText.alpha(flashAlpha);
				break;

			case ANTICIPATION:
				// Slow pulse
				float pulseAlpha = 0.6f + 0.4f * (float) Math.sin(flashTimer * 2f);
				phaseText.alpha(pulseAlpha);
				break;

			case ASSAULT:
			case FADE: // fade looks the same as assault
				// Intense pulse
				float assaultAlpha = 0.7f + 0.3f * (float) Math.sin(flashTimer * 3f);
				phaseText.alpha(assaultAlpha);
				break;

			default:
				phaseText.alpha(0.8f);
				break;
		}
	}

	private void updatePhaseDisplay(HeistManager.Phase phase) {
		String text;
		int color;

		switch (phase) {
			case CASING:
				text = "STEALTH";
				color = 0x8899AA;
				setColor(0x1A2633);
				break;
			case CONTROL:
				text = "ALARM!";
				color = 0xFFCC00;
				setColor(0x4D3D00);
				break;
			case ANTICIPATION:
				text = "GET READY";
				color = 0xDDAA00;
				setColor(0x3D2D00);
				break;
			case ASSAULT:
			case FADE: // fade appears as assault
				text = "ASSAULT #" + Dungeon.heistManager.assaultWaveCount;
				color = 0xFF3333;
				setColor(0x4D0F0F);
				break;
			default:
				text = "";
				color = 0xFFFFFF;
				break;
		}

		phaseText.text(text);
		phaseText.hardlight(color);
		phaseText.measure();
		setSize(Math.max(80, phaseText.width() + 12), 16);
		layout();
	}
}
