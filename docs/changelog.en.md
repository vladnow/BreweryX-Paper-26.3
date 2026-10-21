## 3.7.1-paper26.3.1-beta.1 — 2026-10-21

- Unofficial port based on BreweryX tag 3.7.1 (343872c66a6f37ec4da420cbf12637b4574d5d21).
- Recognize Minecraft 26.2 and 26.3 explicitly; read the game version through Paper's public API.
- Compile against Paper API 26.3.build.169-beta with Java 25; require API 26.3 in plugin metadata.
- Register configured command aliases through Paper's public command map.
- Disable upstream update checks by default for new installations; preserve existing configuration values.
- Include GPLv3, modification notices and bundled-library license texts.
- Add version regression tests and a disposable real-Paper recipe/storage verification harness.
- Retain upstream brewing functionality and optional integration code; optional integration behavior and real-client gameplay remain untested.
- Beta for Paper only. Folia, Spigot, Purpur and other platforms are not claimed.

The changes, checks and release materials were prepared using generative AI. This is not an official BreweryTeam release.
