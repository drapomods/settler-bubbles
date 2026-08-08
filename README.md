# Settler Bubbles

Settler Bubbles is a Necesse 1.3.2 mod inspired by RimWorld's Interaction Bubbles.
It turns the game's existing settler chatter into readable conversations and adds
short contextual lines for social life, work, needs, mood, weather and combat.

## Features

- Shared, server-selected dialogue in singleplayer and multiplayer.
- Supports recruited settlers and visiting human NPCs.
- Multi-turn conversations that remember whether the topic was a person, animal
  or food, including matching reactions and conclusions.
- 160 personality-aware English lines without external AI services.
- Compact pixel speech bubbles, thought bubbles for needs and mood, and distinct
  combat shouts that follow their speaker.
- Context for hunger, injuries, recreation, strikes, happiness, rain, night,
  visitors, jobs and idle settlers.
- Enabled automatically on every game start.
- Session toggle with `/bubbles` or `/bubbles on|off`.
- Configurable frequency, distance, duration and event categories.
- A versioned Java API for add-on mods and compatibility integrations.
- No world save data; the mod can be added or removed safely.

## Java API

The public API is in `draporid.settlerbubbles.api`. Add-on mods should declare
`draporid.settlerbubbles` as a dependency and can check
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
the speaker's level.

## Development

- `gradlew.bat buildModJar` builds the distributable jar.
- `gradlew.bat runClient` launches the normal development client.
- `gradlew.bat runDevClient` launches a second client.
- `gradlew.bat runServer` launches a dedicated server.
