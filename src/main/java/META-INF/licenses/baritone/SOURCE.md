# Baritone 26.2 bundled component

- Upstream: https://github.com/cabaletta/baritone
- 26.2 port: https://github.com/cabaletta/baritone/pull/5076
- Pinned commit: `57758940790254245514b545618b4c8adc7f2ff4`
- License: LGPL-3.0-or-later
- Bundled artifact:
  `bundled-mods/baritone-fabric-1.15.0-10-g57758940+mc26.2-blade1.jar`
- SHA-256:
  `32f5c1513b84039b6ae095085425ea6d3c05617010a89d93c8265e67ac66146c`

The artifact is built with Java 25 using:

```text
bash gradlew :fabric:remapJar
```

The upstream source applies cleanly to Minecraft 26.2/Mojmap. The bundled
variant makes fourteen source changes:

1. `fabric/build.gradle` no longer nests `dev.babbaj:nether-pathfinder`.
   That project does not publish a redistribution license.
2. `baritone.Baritone` registers `NullElytraProcess` directly. Ground
   navigation, mining, building, farming, and goal processes remain enabled;
   native Elytra navigation is intentionally unavailable.
3. `BlockOptionalMeta` uses the block item for client-side drop identity.
   Fabric loot hooks require a real `MinecraftServer`; evaluating them through
   Baritone's client-only `ServerLevel` stub throws during registry access.
4. `Settings.mineSearchRadius` and `MineProcess` provide a current-position
   radius cap. AutoMine sets it to 15 blocks, including cached targets.
5. `Settings.allowRightClick` disables Baritone block interaction. The strict
   AutoMine profile also excludes doors, gates and trapdoors from paths.
6. `IMineProcess.knownTargets()` exposes a read-only diagnostic snapshot so
   AutoMine logs can compare client-visible diamonds with accepted goals.
7. `mineRegionMin*` and `mineRegionMax*` settings constrain accepted mining
   targets to the selected server mine profile.
8. Radius-limited mining scans only currently loaded chunks and allows one
   asynchronous rescan at a time. This avoids stale server-mine cache targets,
   continuous path replacement and render starvation.
9. `MovementHelper` treats every block outside the configured mine region as
   unbreakable. Walking through existing openings remains allowed, but tunnel
   construction cannot damage server-protected blocks outside the arena.
10. `MineProcess` reuses an active path while its target-position set is
    unchanged instead of replacing the same goal every tick. This prevents
    duplicate path calculations and repeated unreachable-target spam.
11. Hot movement/path diagnostics use Baritone's debug logger instead of
    writing to stdout every tick, avoiding render-thread console stalls.
12. `Settings.mineAoeLevel` coalesces ore goals covered by FunTime
    `Бульдозер I` (`3x3x1`) and `Бульдозер II` (`3x3x3`) hits. Level II
    approach goals stop one layer earlier above or below the ore and can break
    a reachable anchor block whose AoE includes the otherwise out-of-reach ore.
13. Placement helpers and non-ladder pillar movement fail before selecting a
    throwaway block whenever `allowPlace` is disabled.
14. `MovementAscend` centers Bulldozer II clearance breaks on the third block
    above a stair, preserving the first block as the support step.

Equivalent patch:

```diff
diff --git a/fabric/build.gradle b/fabric/build.gradle
@@
-    include "dev.babbaj:nether-pathfinder:${project.nether_pathfinder_version}"

diff --git a/src/main/java/baritone/Baritone.java b/src/main/java/baritone/Baritone.java
@@
 import baritone.process.*;
+import baritone.process.elytra.NullElytraProcess;
@@
-            this.elytraProcess = this.registerProcess(ElytraProcess::create);
+            this.elytraProcess = this.registerProcess(NullElytraProcess::new);

diff --git a/src/api/java/baritone/api/utils/BlockOptionalMeta.java b/src/api/java/baritone/api/utils/BlockOptionalMeta.java
@@
-            // evaluate the block loot table through ServerLevelStub
+            Item item = block.asItem();
+            return item == Items.AIR ? Collections.emptyList() : List.of(item);

diff --git a/src/api/java/baritone/api/Settings.java b/src/api/java/baritone/api/Settings.java
@@
+    public final Setting<Integer> mineSearchRadius = new Setting<>(0);
+    public final Setting<Integer> mineAoeLevel = new Setting<>(0);
+    public final Setting<Integer> mineRegionMinX = new Setting<>(Integer.MIN_VALUE);
+    // Equivalent Y/Z minimum and X/Y/Z maximum settings.

diff --git a/src/main/java/baritone/process/MineProcess.java b/src/main/java/baritone/process/MineProcess.java
@@
+        .filter(pos -> searchRadius == 0 || searchCenter.distSqr(pos) <= searchRadiusSq)
+        .filter(MineProcess::insideConfiguredMineRegion)
+    // Local-radius mining scans live chunks and serializes async rescans.
+    // Unchanged target sets keep the active path instead of recalculating.

diff --git a/src/main/java/baritone/utils/InputOverrideHandler.java b/src/main/java/baritone/utils/InputOverrideHandler.java
@@
+        blockPlaceHelper.tick(settings.allowRightClick && clickRight);

diff --git a/src/main/java/baritone/pathing/movement/MovementHelper.java b/src/main/java/baritone/pathing/movement/MovementHelper.java
@@
+        // Return true when x/y/z is outside mineRegionMin*/mineRegionMax*.
```

Users may replace the nested Baritone JAR with an interface-compatible
modified build. Blade accesses it only through the public Baritone API and its
own navigator adapter.
