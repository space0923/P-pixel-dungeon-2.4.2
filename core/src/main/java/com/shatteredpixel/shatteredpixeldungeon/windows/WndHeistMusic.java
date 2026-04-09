/*
 * Payday Pixel Dungeon
 * Copyright (C) 2024 space0923
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeistManager;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

/**
 * Settings sub-window for heist music track configuration.
 * Three modes: BIOME (fixed per biome), RANDOM (per biome), SET (player choice).
 * In SET mode, shows a list of available track packs to choose from.
 */
public class WndHeistMusic extends Window {

	private static final int WIDTH = 120;
	private static final int BTN_HEIGHT = 16;
	private static final float GAP = 2;

	private RedButton btnBiome, btnRandom, btnSet;
	private RenderedTextBlock txtTrackLabel;
	private RedButton[] trackBtns;
	private ColorBlock sepTracks;

	public WndHeistMusic() {
		super();

		int mode = SPDSettings.heistTrackMode();
		int setTrack = SPDSettings.heistSetTrack();
		String[] tracks = Assets.Music.HEIST_TRACKS;

		float pos = 0;

		// --- Title ---
		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, pos);
		add(title);
		pos = title.bottom() + GAP * 2;

		// --- Mode label ---
		RenderedTextBlock modeLabel = PixelScene.renderTextBlock(
				Messages.get(this, "track_mode"), 6);
		modeLabel.hardlight(0xCCCCCC);
		modeLabel.setPos((WIDTH - modeLabel.width()) / 2f, pos);
		add(modeLabel);
		pos = modeLabel.bottom() + GAP;

		// --- Mode buttons (3 across) ---
		int btnWidth = (int) (WIDTH - 2 * GAP) / 3;

		btnBiome = new RedButton(Messages.get(this, "mode_biome")) {
			@Override
			protected void onClick() {
				SPDSettings.heistTrackMode(0);
				updateModeButtons(0);
				updateTrackVisibility(0);
			}
		};
		btnBiome.setRect(0, pos, btnWidth, BTN_HEIGHT - 2);
		add(btnBiome);

		btnRandom = new RedButton(Messages.get(this, "mode_random")) {
			@Override
			protected void onClick() {
				SPDSettings.heistTrackMode(1);
				updateModeButtons(1);
				updateTrackVisibility(1);
			}
		};
		btnRandom.setRect(btnBiome.right() + GAP, pos, btnWidth, BTN_HEIGHT - 2);
		add(btnRandom);

		btnSet = new RedButton(Messages.get(this, "mode_set")) {
			@Override
			protected void onClick() {
				SPDSettings.heistTrackMode(2);
				updateModeButtons(2);
				updateTrackVisibility(2);
			}
		};
		btnSet.setRect(btnRandom.right() + GAP, pos, btnWidth, BTN_HEIGHT - 2);
		add(btnSet);

		pos = btnSet.bottom() + GAP * 2;

		// --- Track selection (for SET mode) ---
		sepTracks = new ColorBlock(WIDTH, 1, 0xFF000000);
		sepTracks.y = pos;
		add(sepTracks);
		pos += 1 + GAP;

		txtTrackLabel = PixelScene.renderTextBlock(
				Messages.get(this, "select_track"), 6);
		txtTrackLabel.hardlight(0xCCCCCC);
		txtTrackLabel.setPos((WIDTH - txtTrackLabel.width()) / 2f, pos);
		add(txtTrackLabel);
		pos = txtTrackLabel.bottom() + GAP;

		trackBtns = new RedButton[tracks.length];
		for (int i = 0; i < tracks.length; i++) {
			final int idx = i;
			trackBtns[i] = new RedButton(HeistManager.trackDisplayName(i)) {
				@Override
				protected void onClick() {
					SPDSettings.heistSetTrack(idx);
					updateTrackButtons(idx);
				}
			};
			trackBtns[i].setRect(0, pos, WIDTH, BTN_HEIGHT - 2);
			add(trackBtns[i]);
			pos = trackBtns[i].bottom() + GAP;
		}

		// --- Initialize visual state ---
		updateModeButtons(mode);
		updateTrackVisibility(mode);
		updateTrackButtons(setTrack);

		resize(WIDTH, (int) Math.ceil(pos));
	}

	private void updateModeButtons(int mode) {
		btnBiome.textColor(mode == 0 ? TITLE_COLOR : WHITE);
		btnRandom.textColor(mode == 1 ? TITLE_COLOR : WHITE);
		btnSet.textColor(mode == 2 ? TITLE_COLOR : WHITE);
	}

	private void updateTrackVisibility(int mode) {
		boolean showTracks = (mode == 2); // SET mode
		sepTracks.visible = showTracks;
		txtTrackLabel.visible = showTracks;
		for (RedButton btn : trackBtns) {
			btn.visible = btn.active = showTracks;
		}

		// Resize window based on whether track list is shown
		if (showTracks) {
			float bottom = trackBtns[trackBtns.length - 1].bottom() + GAP;
			resize(WIDTH, (int) Math.ceil(bottom));
		} else {
			float bottom = btnSet.bottom() + GAP * 2;
			resize(WIDTH, (int) Math.ceil(bottom));
		}
	}

	private void updateTrackButtons(int selectedIdx) {
		for (int i = 0; i < trackBtns.length; i++) {
			trackBtns[i].textColor(i == selectedIdx ? TITLE_COLOR : WHITE);
		}
	}
}
