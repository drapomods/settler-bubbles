# Changelog

All notable changes to Settler Bubbles are documented here.

## [1.2.0] - 2026-08-17

### Added

- Session-only social memory: settlers remember earlier conversation partners,
  subjects and outcomes without writing anything to the world save.
- Stable personal opinions about villagers, animals and food, preventing a
  settler from randomly reversing the same opinion in a later conversation.
- Paired reactions to attacks, injuries, food shortages, rain, strikes and
  victories, with a short delay between the original bubble and the witness.
- Dedicated work dialogue for mining, building, cooking, caring, hauling and
  guarding.
- `/bubbles debug`, including effective settings, languages, session-memory
  counts and detection of other mods that may render settler chatter.
- `/bubbles smoke`, a guided fourteen-step visual check of translated speech,
  thought and combat bubbles.
- `gradlew.bat smokeTest` for build-time locale, dialogue-catalog and jar checks.

### Changed

- Repetition protection now considers both each individual speaker and recent
  dialogue across the entire loaded settlement level.
- Event witnesses answer from a nearby settler after a short delay instead of
  producing simultaneous overlapping bubbles.

### Compatibility

- Social memories and opinions exist only for the current play session. World
  saves and API version 1 remain unchanged and compatible.

## [1.1.2] - 2026-08-12

### Fixed

- Removed the repetitive generic busy-work chatter that could still appear in
  English for translated clients. Contextual job-completion lines remain.
- Base-game settler chat now shares the Settler Bubbles renderer, preventing it
  from overlapping a mod dialogue or combat bubble on the same settler.
- A lower-priority bubble can no longer replace an active combat, needs or mood
  bubble from the same speaker.
- Manually selected Korean, Japanese and Chinese bubble languages now load the
  characters used by each message, even when the game interface uses another
  language. This prevents missing glyphs from appearing as question marks.

## [1.1.1] - 2026-08-12

### Fixed

- Busy-work bubbles no longer embed base-game activity messages. This prevents
  untranslated Necesse activity keys from appearing as English fragments when
  using Korean or another translated bubble language.
- The affected short work lines are now fully owned and translated by Settler
  Bubbles in every supported language.

## [1.1.0] - 2026-08-10

### Added

- In-game settings menu opened with `/bubbles settings`.
- Optional rebindable settings shortcut, left unbound by default to prevent
  conflicts with other mods.
- Automatic first-run font sizing for the current effective resolution.
- Selectable 14-22 px text sizes and Game Default, Pixel and Smooth font modes.
- Smart bubble density with configurable limits and priority for combat, needs
  and nearby speakers.
- Brazilian Portuguese, German, Spanish, French, Dutch, Polish, Russian,
  Simplified Chinese, Japanese and Korean translations.
- Manual bubble-language selection independent of the game's interface language.
- Build-time locale validation for keys and replacement placeholders.

### Changed

- Bubble width now scales with font size to keep translated dialogue readable.
- Client visual and density preferences now also apply to API-provided bubbles.
- All included translations received a semantic review for mistranslated context
  and overly literal wording.

### Compatibility

- Existing settings migrate automatically; saves and API version 1 remain
  compatible.

## [1.0.0] - 2026-08-08

### Added

- Synchronized settler and visitor speech bubbles for singleplayer and multiplayer.
- Multi-turn conversations with contextual reactions and conclusions.
- 160 personality-aware English dialogue lines.
- Thought bubbles for needs and mood, plus distinct combat shouts.
- Dialogue for work, social life, hunger, injuries, recreation, strikes, weather,
  night, visitors and combat.
- Settings for frequency, distance, duration and event categories.
- Session commands: `/bubbles`, `/bubbles on` and `/bubbles off`.
- Versioned Java API for add-on mods and compatibility integrations.
