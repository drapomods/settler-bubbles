# Settler Bubbles

![Settler Bubbles](src/main/resources/preview.png)

Settler Bubbles is a Necesse 1.3.2 mod inspired by RimWorld's Interaction
Bubbles. It turns existing settler chatter into readable conversations and adds
short contextual lines for social life, work, needs, mood, weather and combat.

Created by [DrapoMods](https://github.com/drapomods).

## Features

- Shared, server-selected dialogue in singleplayer and multiplayer.
- Supports recruited settlers and visiting human NPCs.
- Multi-turn conversations that remember whether the topic was a person, animal
  or food, including matching reactions and conclusions.
- Session-only social memory and stable opinions about villagers, animals and
  food, including callbacks to earlier conversations.
- More than 190 personality-aware dialogue lines in English plus 10 complete
  translations.
- Speech bubbles with automatic first-run text scaling, selectable font size and
  font style, thought bubbles for needs and mood, and distinct combat shouts.
- Context for hunger, injuries, recreation, strikes, happiness, rain, night,
  visitors, jobs and idle settlers.
- Nearby settlers react to attacks, injuries, food shortages, rain, strikes and
  victories instead of every event remaining an isolated thought.
- Dedicated work lines for farming, fishing, crafting, woodcutting, mining,
  building, cooking, caring, hauling and guarding.
- Enabled automatically on every game start.
- Session toggle with `/bubbles` or `/bubbles on|off`.
- In-game settings menu opened with `/bubbles settings`, plus an optional
  shortcut that can be assigned in Necesse's Controls settings.
- Configurable frequency, distance, duration, font, event categories and smart
  bubble density.
- A versioned Java API for add-on mods and compatibility integrations.
- No world save data; the mod can be added or removed safely.

## Requirements and installation

Settler Bubbles 1.2.0 targets Necesse 1.3.2. In multiplayer, install the same
mod version on the host or dedicated server and on every connecting client.

Subscribe through the [Steam Workshop](https://steamcommunity.com/sharedfiles/filedetails/?id=3779949320),
or place the release jar manually in
`%APPDATA%\Necesse\mods\` and enable Settler Bubbles in the game's Mods menu.

## Commands and settings

- `/bubbles` toggles bubbles for the current play session.
- `/bubbles on` enables them for the current session.
- `/bubbles off` disables them for the current session.
- `/bubbles settings` opens the settings menu.
- `/bubbles debug` prints the effective settings, language, session-memory
  counts and possible chatter-mod conflicts.
- `/bubbles smoke` starts or cancels a guided visual test of every bubble style
  and the new dialogue groups. The full sequence takes about 49 seconds.

The settings shortcut is unbound by default to avoid conflicts with other mods.
Players can assign any preferred key in Necesse's Controls settings. Frequency,
visibility distance, duration, font size, font style, bubble language, dialogue
categories and the maximum visible bubble count can all be adjusted. The first
launch picks a font size for the current resolution; players can then override
it or run auto-detection again. Bubbles start enabled, so no command is required
after launch.

## Languages

Dialogue follows the language selected in Necesse by default and can be
overridden manually in the mod settings. Version 1.2.0 includes English,
Brazilian Portuguese, German, Spanish, French, Dutch, Polish, Russian,
Simplified Chinese, Japanese and Korean. Unsupported languages fall back to
English when the game-language option is active.

Translation corrections are welcome through
[GitHub Issues](https://github.com/drapomods/settler-bubbles/issues). Please
include the language, translation key, current text and suggested replacement.

## Java API

The public API is in `drapomods.settlerbubbles.api`. Add-on mods should declare
`drapomods.settlerbubbles` as a dependency and check
`SettlerBubblesAPI.API_VERSION` before registering integrations.

Show one bubble directly from server-side code:

```java
SettlerBubblesAPI.showBubble(BubbleRequest.builder(
        settler,
        new LocalMessage("mymod", "foundtreasure")
    )
    .category(BubbleCategory.MOOD)
    .style(BubbleStyle.THOUGHT)
    .duration(4000)
    .cooldown(5000)
    .build());
```

Providers let multiple mods contribute weighted lines to the same namespaced
event. Keep the returned handle if the provider may need to be unregistered:

```java
RegistrationHandle handle = SettlerBubblesAPI.registerProvider(
    "mymod:treasure_dialogue",
    context -> {
        if (!context.getTriggerID().equals("mymod:found_treasure")) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
            BubbleLine.local("mymod", "treasurereaction")
                .weight(10)
                .duration(4200)
                .style(BubbleStyle.SPEECH)
                .build()
        );
    }
);

SettlerBubblesAPI.fireEvent(BubbleContext.builder(
        "mymod:found_treasure", settler)
    .category(BubbleCategory.MOOD)
    .attribute("rarity", "legendary")
    .build());
```

`showBubble` and `fireEvent` must run on the server. The API applies category
settings and cooldowns and synchronizes the selected bubble to all clients on
the speaker's level. Client font, language and density preferences also apply
to bubbles supplied through the API. API version 1 remains compatible with
Settler Bubbles 1.0.0 add-ons.

## Development

Copy `gradle.properties.example` to `gradle.properties` and set `necesseDir` to
your Necesse installation. You can instead pass `-PnecesseDir=...` or set the
`NECESSE_DIR` environment variable. Common Windows Steam locations are detected
automatically.

- `gradlew.bat buildModJar` builds the distributable jar.
- `gradlew.bat runClient` launches the normal development client.
- `gradlew.bat runDevClient` launches a second client.
- `gradlew.bat runServer` launches a dedicated server.
- `gradlew.bat smokeTest` builds the release and checks all locale keys,
  placeholders, required dialogue groups and required jar contents.

## Support

- Report bugs or request features through [GitHub Issues](https://github.com/drapomods/settler-bubbles/issues).
- Download or subscribe through the [Steam Workshop](https://steamcommunity.com/sharedfiles/filedetails/?id=3779949320).
- Private contact: [drapomods@proton.me](mailto:drapomods@proton.me)
- Reddit: [u/DrapoMods](https://www.reddit.com/user/DrapoMods/)
- Discord: `DrapoMods`

When reporting a bug, include the Settler Bubbles version, Necesse version,
singleplayer/server type, reproduction steps, other installed mods and the
relevant part of `%APPDATA%\Necesse\latest-log.txt`.

## License and disclaimer

Settler Bubbles is source-available under the
[Settler Bubbles Source License](LICENSE). You may inspect the source and build
independent add-ons through the documented public API. Copying, reuploading,
commercial exploitation and publishing modified versions or forks are not
permitted without prior written permission from DrapoMods. The logo, preview
and DrapoMods branding have additional protection under the
[artwork and branding license](ASSET-LICENSE.md).

Settler Bubbles is an unofficial fan-made mod and is not affiliated with or
endorsed by Fair Games or Ludeon Studios. Necesse, RimWorld and their respective
names and trademarks belong to their owners.
