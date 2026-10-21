# Brewery Continuum — unofficial BreweryX port

BreweryX 3.7.1 compatibility port for **Paper 26.3 / Java 25**.
Version: `3.7.1-paper26.3.1-beta.1`. Beta; not an official BreweryTeam release.

Upstream: https://github.com/BreweryTeam/BreweryX/tree/3.7.1
Base commit: `343872c66a6f37ec4da420cbf12637b4574d5d21`.
The tag's upstream build script still said 3.7.0; this port uses its own explicit version.

The plugin provides configurable cauldron brewing, distillation, barrel aging
and drink effects inherited from BreweryX. Original authors: Grafe, TTTheKing,
Sn0wStorm; BreweryTeam and the contributors named in the original source and
plugin metadata retain their copyright.

## Changes and checks

Explicit recognition of 26.2 and 26.3, Paper version and command-map APIs,
Java 25 and Paper 26.3 build target, upstream update checks off by default for
new configurations, regression checks and license notices.
Eight version regression tests pass. The disposable real-Paper harness checks
recipe items, PDC serialization, fermentation/distillation/aging/sealing code,
cauldron particles and SQLite persistence. See the accompanying verification
report for exact results. Real-client interaction, multiplayer and optional
integration behavior have not been validated. Folia and Spigot are not claimed.

## Build from complete sources

Install JDK 25 and set JAVA_HOME to it. PowerShell 7 is recommended.
Run `./build.ps1` (or `./prepare-apis.ps1`, then `./gradlew.bat clean build sourcesJar`).
The first build requires network access to the public repositories in
build.gradle.kts. No publishing token is required. Two compile-only APIs are
downloaded from pinned official URLs and verified by SHA-256. They are excluded
from this source bundle and are never shaded into the runtime plugin.

On Windows with Cyrillic paths, use build.ps1: it selects the Windows argument
file encoding for Gradle workers while Java resources remain UTF-8. On other
systems run Gradle 9.6.1 with JDK 25 after preparing the two compile-only files.
Outputs: `build/libs/BreweryX-3.7.1-paper26.3.1-beta.1.jar` and `-sources.jar`.

## Installation

Stop Paper, back up the world and complete plugins/BreweryX folder, replace the
old BreweryX JAR with this one, start Paper 26.3 using Java 25. Use one BreweryX
runtime only. Players need no mod. `/brew help`, `/brew version`, `/brew reload`.
Set `language: ru` in plugins/BreweryX/config.yml for Russian, then reload.
Do not test first on the only copy of an existing server.

Inherited bStats is opt-out: set `enabled: false` in plugins/bStats/config.yml
and restart to disable it. New configs use `resolveUpdatesFrom: NONE`; existing
configs keep their setting. Upstream update checks do not track this fork.

## License and distribution

GNU GPLv3; see LICENSE.txt and PORT-NOTICE.md. Preserve author and modification
notices. Publish the matching complete source and build files alongside the JAR.
Dependencies retain their own licenses in META-INF/third-party-licenses.
Port changes, tests and publication materials were prepared using generative AI;
this statement makes no claim about AI use in upstream BreweryX.

The future publisher must add the actual public source and issue links here.
Platform admission is separate from the GPL permission; in particular current
Modrinth AI-fork rules can prohibit public listing of this port, and Hangar and
SpigotMC impose originality requirements. No external publication has occurred.
