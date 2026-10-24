# Unofficial BreweryX port

Modified on 2026-10-21 for Paper 26.3 / Java 25.
Based on BreweryTeam/BreweryX tag 3.7.1, commit 343872c66a6f37ec4da420cbf12637b4574d5d21.
Upstream: https://github.com/BreweryTeam/BreweryX
Original Brewery authors: Grafe, TTTheKing, Sn0wStorm.
BreweryX authors and contributors retain their original copyright notices.
This is an unofficial compatibility port, not an official BreweryTeam release.

Changes: explicit calendar version detection including 26.3, Paper game-version
and command-map APIs, Paper 26.3 compile target and Java 25, regression checks,
upstream update checks off by default on new installations, publication and
license notices. Folia and other platforms are not claimed for this build.
The port changes, tests and publication materials were prepared with generative AI.
This statement concerns this port and does not attribute AI use to upstream.

Distributed under GNU GPL version 3. Preserve LICENSE.txt, original attribution,
this modification notice and dependency notices. When distributing the binary,
provide the matching complete source and build files under GPLv3 alongside it.
Included libraries retain their own compatible licenses. Compile-only plugin
APIs are not bundled into the runtime plugin.

## Localization update — 2026-10-23

Version 3.7.1-paper26.3.1-beta.2 extends the inherited English/Russian language
selection: 21 formerly hard-coded command messages now use the language files,
the config explains language: en / language: ru and /brew reload, locale aliases
are normalized, and unavailable locales safely fall back to English.
An inherited English default in the Russian language file is migrated only when
its old value is unchanged. Custom administrator messages are preserved.
The original Russian translation authors retain credit in the language files.
These changes and verification were prepared with generative AI under GPLv3.

## Full standard-content localization — 2026-10-23

Version 3.7.1-paper26.3.1-beta.3 adds Russian presentation catalogs for all
23 enabled standard drinks, the disabled example, and 35 cauldron defaults.
Stock names, lore and consumption messages follow language: en / ru without
changing the persisted recipe identity or recipe mechanics. Existing tagged
bottles and sealing-table items have their text refreshed on inventory access
and language reload; custom item names and unrelated metadata are preserved.
Remaining player-facing protection-integration errors use language entries.
This update, translation and checks were prepared with generative AI under
GPLv3. Real-client gameplay and optional third-party integrations are untested.

## Five additional recipes — 2026-10-23

Version 3.7.1-paper26.3.1-beta.4 adds Liquid Crystal, Jägermeister, Crimson Drink,
Royal Drink and Wildwood Mixture using the server owner's requested ingredients,
strengths, timing and effects. A one-time migration adds missing IDs while
preserving existing/disabled/legacy recipes and making a recipe backup on upgrade.
Names follow the existing English/Russian presentation catalogs. Intermediate
floating-point alcohol arithmetic avoids losing one point from odd strengths
when a brew completes the specified distillation runs. These changes and checks
were prepared with generative AI under GPLv3. This is a local beta; no real-client
playtest or remote publication is claimed.

## Short grass correction — 2026-10-23

Version 3.7.1-paper26.3.1-beta.5 changes Jägermeister and Wildwood Mixture
from TALL_GRASS to SHORT_GRASS at the server owner's request. One-time backed-up
migration updates only that material in those two IDs in recipes.yml and legacy
config.yml recipes, retaining quantities, other settings and disabled status.
Subsequent administrator edits are preserved. Prepared with generative AI under GPLv3.
