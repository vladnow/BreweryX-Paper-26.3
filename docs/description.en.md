# Brewery Continuum

An unofficial compatibility port of BreweryX 3.7.1 for Paper 26.3 and Java 25. BreweryX adds a configurable brewing process with ingredient cooking in cauldrons, distillation, barrel aging, custom recipes and drink effects.

This port explicitly recognizes Minecraft 26.3, compiles against its Paper API, uses Paper's public game-version and command-map APIs, and disables upstream release checks by default for new configurations. The technical plugin name remains BreweryX so its configuration and data folder remain compatible.

## Installation

Stop the server and back up its world and BreweryX data, including SQLite files. Remove the previous BreweryX runtime JAR, put the port JAR into `plugins`, then start Paper 26.3 with Java 25. Install only one BreweryX implementation at a time. Players do not need a client mod. Use `/brew help` and `/brew version` to begin. To use Russian, set `language: ru` in `plugins/BreweryX/config.yml` and run `/brew reload`.

## Status

Beta. See the included verification report for the exact tested Paper build and automated coverage. Real-client gameplay, multiplayer behavior, Folia, other server platforms and optional third-party plugin integrations have not been validated. Automated recipe and storage checks do not establish complete gameplay compatibility.

## Attribution and license

Based on [BreweryTeam/BreweryX](https://github.com/BreweryTeam/BreweryX), tag 3.7.1, commit 343872c66a6f37ec4da420cbf12637b4574d5d21. Credit belongs to the BreweryTeam contributors and original Brewery authors Grafe, TTTheKing and Sn0wStorm. This is an independent port and is not an official BreweryTeam release. Distributed under GNU GPLv3; preserve the included copyright and dependency notices. Matching complete source and build files accompany the release.

The compatibility changes, tests and publication text in this port were prepared using generative AI. This disclosure concerns the port; it makes no claim about AI use in the upstream project.

## Connections and telemetry

Inherited bStats telemetry is enabled by default and can be disabled with `enabled: false` in `plugins/bStats/config.yml`, followed by a server restart. This fork defaults new configurations to `resolveUpdatesFrom: NONE`. Existing configurations retain their settings; choosing GITHUB, SNAPSHOTS or SPIGOT enables the corresponding upstream update-check connections. Updates are checked, not automatically installed.

Original documentation: [BreweryTeam docs](https://docs.breweryteam.dev/). Report port-specific issues to the port's own repository.
