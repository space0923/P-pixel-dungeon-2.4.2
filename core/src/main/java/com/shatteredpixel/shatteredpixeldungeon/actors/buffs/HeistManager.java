/*
 * Payday Pixel Dungeon
 * Copyright (C) 2024 space0923
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ShopRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Random;

public class HeistManager implements Bundlable {

	public enum Phase {
		STEALTH,
		CONTROL,
		ANTICIPATION,
		ASSAULT,
		FADE
	}

	/**
	 * Track play modes:
	 * BIOME  - fixed track per biome (biomeIndex % trackCount)
	 * RANDOM - random track picked once per biome, persisted for all 5 levels
	 * SET    - player-picked track used for all biomes
	 */
	public enum TrackMode {
		BIOME,    // 0
		RANDOM,   // 1
		SET       // 2
	}

	public Phase phase = Phase.STEALTH;
	public float phaseTimer = 0;        // seconds remaining in current phase
	public int assaultWaveCount = 0;    // how many assault waves this biome
	public boolean inShop = false;      // whether hero is currently in a shop room
	public int currentTrackIdx = 0;     // resolved track index for current biome

	// Phase durations (in seconds)
	private static final float CONTROL_DURATION = 20f;
	private static final float ANTICIPATION_MIN = 25f;
	private static final float ANTICIPATION_MAX = 30f;
	private static final float ASSAULT_MIN = 110f;
	private static final float ASSAULT_MAX = 120f;
	private static final float FADE_MIN = 45f;
	private static final float FADE_MAX = 80f;

	private static final String BIOME_IDX = "biome_idx";
	public int currentBiome = 0;

	// Returns the current biome index (0-4) for the given depth
	public static int biomeFor(int depth) {
		return (depth - 1) / 5;
	}

	/**
	 * Resolves which track index should be used for the given biome,
	 * based on the current TrackMode setting.
	 */
	private int resolveTrackIdx(int biome) {
		if (Assets.Music.HEIST_TRACKS == null) return 0;
		int trackCount = Assets.Music.HEIST_TRACKS.length;
		if (trackCount == 0) return 0;

		TrackMode mode = TrackMode.values()[SPDSettings.heistTrackMode()];
		switch (mode) {
			case BIOME:
				return biome % trackCount;
			case RANDOM:
				return Random.Int(trackCount);
			case SET:
				int setIdx = SPDSettings.heistSetTrack();
				return Math.min(setIdx, trackCount - 1);
			default:
				return 0;
		}
	}

	/**
	 * Called once when a new game starts.
	 * Resolves the track for the first biome (depth 1, biome 0).
	 */
	public void init() {
		currentBiome = 0;
		currentTrackIdx = resolveTrackIdx(currentBiome);
	}

	// Called when entering a new floor — resets if new biome
	public void onFloorChange() {
		int newBiome = biomeFor(Dungeon.depth);
		if (currentBiome != newBiome) {
			phase = Phase.STEALTH;
			phaseTimer = 0;
			assaultWaveCount = 0;
			// Resolve track for the new biome
			currentTrackIdx = resolveTrackIdx(newBiome);
			currentBiome = newBiome;
		}
		// Reset shop state on floor change
		inShop = false;
	}

	// Called to trigger the alarm — only works from STEALTH phase
	public void triggerAlarm() {
		if (phase != Phase.STEALTH) return;

		phase = Phase.CONTROL;
		phaseTimer = CONTROL_DURATION;

		// Play alarm SFX
		Sample.INSTANCE.play(Assets.Sounds.ALERT);

		// Alert all mobs on the floor using beckon (public API)
		if (Dungeon.level != null && Dungeon.hero != null) {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (mob.alignment == com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY) {
					mob.beckon(Dungeon.hero.pos);
				}
			}
		}

		GLog.n(Messages.get(HeistManager.class, "alarm_triggered"));

		// Start CONTROL phase music
		playPhaseMusic();
	}

	/**
	 * Called every frame from GameScene.update() to tick the phase timer.
	 * @param elapsed seconds since last frame (Game.elapsed)
	 */
	public void update(float elapsed) {
		if (Dungeon.depth % 5 == 0) {
			return; // Disable assault spawning during boss fights
		}
		
		if (phase == Phase.STEALTH) {
			return; // nothing to do in stealth
		}

		phaseTimer -= elapsed;

		if (phaseTimer <= 0) {
			switch (phase) {
				case CONTROL:
					// Control -> Anticipation
					phase = Phase.ANTICIPATION;
					phaseTimer = Random.Float(ANTICIPATION_MIN, ANTICIPATION_MAX);
					GLog.w(Messages.get(HeistManager.class, "anticipation"));
					playPhaseMusic();
					break;

				case ANTICIPATION:
					// Anticipation -> Assault
					phase = Phase.ASSAULT;
					assaultWaveCount++;
					phaseTimer = Random.Float(ASSAULT_MIN, ASSAULT_MAX);
					spawnAssaultWave();
					GLog.n(Messages.get(HeistManager.class, "assault", assaultWaveCount));
					playPhaseMusic();
					break;

				case ASSAULT:
					// Assault -> Fade (hidden sub-state, no music/HUD change)
					phase = Phase.FADE;
					phaseTimer = Random.Float(FADE_MIN, FADE_MAX);
					// No music switch or log — fade is part of assault visually
					break;

				case FADE:
					// Fade -> Control (cycle repeats from control)
					phase = Phase.CONTROL;
					phaseTimer = CONTROL_DURATION;
					GLog.w(Messages.get(HeistManager.class, "control"));
					playPhaseMusic();
					break;
			}
		}
	}

	// Spawns a batch of extra mobs at the start of an assault wave
	private void spawnAssaultWave() {
		if (Dungeon.level == null) return;

		// Number of extra mobs: 2-4, plus 1 per wave (capped at 6 extra)
		int extras = Math.min(6, Random.IntRange(2, 4) + (assaultWaveCount - 1));

		for (int i = 0; i < extras; i++) {
			if (Dungeon.level.spawnMob(12) || Dungeon.level.spawnMob(6)) {
				// spawnMob will handle HUNTING state + AssaultAlert buff via Level.java
			}
		}
	}

	/**
	 * Plays the current track's intro+loop pair for the current heist phase.
	 * Uses the resolved currentTrackIdx which was set on biome entry.
	 */
	public void playPhaseMusic() {
		// Don't change music while in shop
		if (inShop) return;

		String phaseName;
		switch (phase) {
			case STEALTH:      phaseName = "stealth";      break;
			case CONTROL:      phaseName = "control";      break;
			case ANTICIPATION: phaseName = "anticipation"; break;
			case ASSAULT:
			case FADE:         phaseName = "assault";      break;
			default: return;
		}

		playCurrentTrack(phaseName);
	}

	/**
	 * Plays the intro+loop for the given phase name from the current track folder.
	 */
	private void playCurrentTrack(String phaseName) {
		int trackCount = Assets.Music.HEIST_TRACKS.length;
		if (trackCount == 0) return;

		int idx = Math.min(currentTrackIdx, trackCount - 1);
		final String track = Assets.Music.HEIST_TRACKS[idx];
		final String intro = Assets.Music.HEIST_MUSIC_DIR + track + "/" + phaseName + "_intro.ogg";
		final String loop  = Assets.Music.HEIST_MUSIC_DIR + track + "/" + phaseName + "_loop.ogg";
		com.watabou.noosa.Game.runOnRenderThread(new com.watabou.utils.Callback() {
			@Override
			public void call() {
				Music.INSTANCE.playWithIntro(intro, loop);
			}
		});
	}

	// Check if hero is in a shop room — handle music transitions
	public void checkShopPresence() {
		boolean wasInShop = inShop;

		if (Dungeon.level instanceof RegularLevel && Dungeon.hero != null) {
			Room room = ((RegularLevel) Dungeon.level).room(Dungeon.hero.pos);
			inShop = (room instanceof ShopRoom);
		} else {
			inShop = false;
		}

		if (inShop && !wasInShop) {
			// Entered shop — play shared shop intro+loop
			final String intro = Assets.Music.HEIST_MUSIC_DIR + "shop_intro.ogg";
			final String loop  = Assets.Music.HEIST_MUSIC_DIR + "shop_loop.ogg";
			com.watabou.noosa.Game.runOnRenderThread(new com.watabou.utils.Callback() {
				@Override
				public void call() {
					Music.INSTANCE.playWithIntro(intro, loop);
				}
			});
		} else if (!inShop && wasInShop) {
			// Left shop — resume current phase music
			playPhaseMusic();
		}
	}

	// Whether the respawner should be active (only during ASSAULT)
	public boolean respawnerActive() {
		return phase == Phase.ASSAULT;
	}

	/**
	 * Returns the display name for a track folder.
	 * Converts folder names like "default_heist" -> "Default Heist"
	 */
	public static String trackDisplayName(int idx) {
		if (idx < 0 || idx >= Assets.Music.HEIST_TRACKS.length) return "???";
		String raw = Assets.Music.HEIST_TRACKS[idx];
		// Capitalize each word, replace underscores with spaces
		StringBuilder sb = new StringBuilder();
		for (String word : raw.split("_")) {
			if (sb.length() > 0) sb.append(' ');
			if (word.length() > 0) {
				sb.append(Character.toUpperCase(word.charAt(0)));
				if (word.length() > 1) sb.append(word.substring(1));
			}
		}
		return sb.toString();
	}

	// --- Save/Load ---

	private static final String PHASE = "phase";
	private static final String PHASE_TIMER = "phase_timer";
	private static final String WAVE_COUNT = "wave_count";
	private static final String IN_SHOP = "in_shop";
	private static final String TRACK_IDX = "track_idx";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(PHASE, phase);
		bundle.put(PHASE_TIMER, phaseTimer);
		bundle.put(WAVE_COUNT, assaultWaveCount);
		bundle.put(IN_SHOP, inShop);
		bundle.put(TRACK_IDX, currentTrackIdx);
		bundle.put(BIOME_IDX, currentBiome);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		phase = bundle.getEnum(PHASE, Phase.class);
		if (phase == null) phase = Phase.STEALTH;
		phaseTimer = bundle.getFloat(PHASE_TIMER);
		assaultWaveCount = bundle.getInt(WAVE_COUNT);
		inShop = bundle.getBoolean(IN_SHOP);
		currentTrackIdx = bundle.getInt(TRACK_IDX);
		currentBiome = bundle.getInt(BIOME_IDX);
	}
}
