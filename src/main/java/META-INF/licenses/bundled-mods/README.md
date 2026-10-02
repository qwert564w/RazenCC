# Bundled runtime mods

The files in `bundled-mods/` are pinned, unmodified Fabric artifacts selected
for Minecraft 1.21.11. `manifest.json` records the exact Modrinth project and
version IDs, download URLs, SHA-1/SHA-512 hashes, sizes, source links, and
licenses. The Gradle `verifyBundledMods` task rejects missing, unexpected, or
modified files before packaging.

Each nested JAR retains its own license/notice files. License identifiers and
upstream license links are also recorded in the manifest. These artifacts are
separate third-party components and are not relicensed as Ryzen code.

Text Placeholder API is included because Mod Menu 17.0.1-beta.1 declares it as
a required dependency. Its filename contains `1.21.10`, but Modrinth explicitly
marks version `qxjzQ9xY` compatible with Minecraft 1.21.11.

Figura backs the ClickGUI Cosmetics page: the avatars under
`config/ryzen/cosmetics` are `.bbmodel` plus Lua, and nothing else can render
them. It is the one entry not resolved through the Modrinth API — the pinned
1.21.11 artifact was vendored from the Polaris reference client — so its
manifest entry carries no version id or download URL.

Note that Figura declares a soft conflict with ImmediatelyFast, which is also
bundled. Fabric warns but still loads both. If avatars render incorrectly,
drop `ImmediatelyFast-Fabric-1.14.3+1.21.11.jar` and its manifest entry.

Emojiful and Blade's patched Baritone are intentionally absent. See their
status files under `third-party/`.
