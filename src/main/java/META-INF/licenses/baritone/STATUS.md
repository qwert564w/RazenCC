# Baritone status for Minecraft 1.21.11

No runtime Baritone JAR is bundled yet.

BladeReload used a custom Minecraft 26.2/Java 25 build with fourteen local
changes documented in `SOURCE.md`. That binary cannot load on Java 21 and must
not be presented as a Minecraft 1.21.11 dependency. A stock 1.21.8 API JAR in
`libs/` is retained only as a temporary compile-time migration scaffold; it is
never added to `localRuntime`, `META-INF/jars`, or the release manifest.

The final compatible artifact must:

- target Minecraft 1.21.11 and Java 21;
- use the Fabric mod id `baritone`;
- preserve all fourteen changes in `SOURCE.md`, including the custom mining
  settings and `IMineProcess.knownTargets()` API;
- be placed in `bundled-mods/` with a filename beginning
  `baritone-fabric-`;
- receive a pinned source revision, URL, SHA-512, and license record before it
  is accepted as a release dependency.

Until then, Baritone-backed features are not production-ready.
