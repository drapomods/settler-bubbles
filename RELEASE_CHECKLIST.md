# Settler Bubbles release checklist

## Repository and package

- [x] Confirm the mod ID, author, Necesse version and release version in `build.gradle`.
- [x] Check the logo/preview, README, changelog and licenses.
- [x] Run `gradlew.bat clean buildModJar` from a clean checkout.
- [x] Open the generated jar and confirm `mod.info`, `preview.png` and compiled classes are present.

## In-game tests

- [ ] Start a new singleplayer world and load an existing world.
- [ ] Confirm bubbles are enabled automatically.
- [ ] Check speech, conversation, thought and combat bubbles.
- [ ] Check hunger, injury, mood, work, social, visitor, weather and night situations.
- [ ] Test `/bubbles`, `/bubbles on` and `/bubbles off`.
- [ ] Test frequency, distance, duration and category settings.
- [ ] Check common UI scales and screen resolutions for overlap and clipping.
- [ ] Test a hosted multiplayer game with both host and client running the same version.
- [ ] Start a dedicated server and connect a client with the same version.
- [ ] Confirm the world still loads after disabling or removing the mod.

## Private publication test

- [ ] Upload the jar, preview, description and screenshots to a hidden Steam Workshop item.
- [x] Subscribe on a normal client and confirm Workshop installation works without local development files.
- [ ] Re-test singleplayer and multiplayer from the Workshop installation.

## Public release

- [ ] Merge the release preparation pull request.
- [ ] Create the `v1.0.0` Git tag and GitHub release with the tested jar attached.
- [ ] Make the Steam Workshop item public.
- [ ] Publish the announcement and monitor GitHub Issues, Reddit, Discord and email.
