package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10690;
import net.minecraft.class_10691;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1536;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1667;
import net.minecraft.class_1670;
import net.minecraft.class_1671;
import net.minecraft.class_1672;
import net.minecraft.class_1673;
import net.minecraft.class_1675;
import net.minecraft.class_1676;
import net.minecraft.class_1678;
import net.minecraft.class_1679;
import net.minecraft.class_1680;
import net.minecraft.class_1681;
import net.minecraft.class_1683;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1686;
import net.minecraft.class_1687;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1803;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_1844;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3855;
import net.minecraft.class_3856;
import net.minecraft.class_3857;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_9236;
import net.minecraft.class_9278;
import net.minecraft.class_9334;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.ScaleUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.ryzen.utils.render.world.WorldMeshRenderer;

@Environment(EnvType.CLIENT)
public final class TrajectoriesFeature extends Feature implements MinecraftContext {
   private static final String TARGET_SELF = "Self";
   private static final String TARGET_PLAYERS = "Players";
   private final MultiSelectSetting predictTrajectory = this.register(new MultiSelectSetting("Predict Trajectory", Set.of("Self"), "Self", "Players"));
   private final BooleanSetting showOwner = this.register(new BooleanSetting("Show Owner", true));
   private final List<TrajectoriesFeature.ScreenLine> pendingScreenLines = new ArrayList<>();
   private final List<TrajectoriesFeature.ImpactPoint> points = new ArrayList<>();
   private int tpSkipTicks;
   private static final int MAX_SIMULATION_TICKS = 300;
   private static final float DESIGN_SCALE = 1.6F;
   private static final float SCREEN_CONNECTOR_THICKNESS = 1.35F;
   private static final float TAG_WIDTH = 36.0F;
   private static final float TAG_CARD = 36.0F;
   private static final float TAG_HEIGHT = 48.0F;
   private static final float TAG_GAP = 4.0F;
   private static final float TAG_RADIUS = 10.0F;
   private static final float TAG_BORDER = 1.2F;
   private static final float TAG_PADDING = 6.0F;
   private static final float TAG_ICON = 20.0F;
   private static final float TAG_TEXT_SIZE = 8.0F;
   private static final float TAG_TEXT_ROW = 8.0F;
   private static final int IMPACT_RING_SEGMENTS = 180;
   private static final double IMPACT_RING_HALF_WIDTH = 0.004;
   private static final double IMPACT_CROSS_RADIUS_FACTOR = 0.72;
   private static final double IMPACT_CROSS_HALF_WIDTH = 0.003;
   private static final int POTION_AREA_RING_SEGMENTS = 220;
   private static final double POTION_AREA_RING_HALF_WIDTH = 0.018;
   private static final double POTION_AREA_Y_OFFSET = 0.012;
   private static final double POTION_GROUND_SEARCH_UP = 0.35;
   private static final double POTION_GROUND_SEARCH_DOWN = 2.5;
   private static final double SPLASH_POTION_RADIUS = 4.0;
   private static final double LINGERING_POTION_RADIUS = 3.0;
   private static final double SPLASH_ENTITY_VERTICAL_RANGE = 2.0;
   private static final double SPLASH_MIN_DISTANCE = 0.001;
   private static final double SPLASH_LINE_MIN_ALPHA = 0.38;
   private static final double SPLASH_LINE_MAX_ALPHA = 0.92;
   private static final float POTION_AREA_ALPHA = 0.55F;
   private static final float POTION_AREA_CONNECTOR_ALPHA = 0.67F;
   private static final double ENTITY_HIT_BOX_EXPAND = 0.028;
   private static final float ENTITY_HIT_BOX_ALPHA = 0.94F;
   private static final float ENTITY_HIT_BOX_FILL_ALPHA = 0.09F;
   private static final int SPLASH_LINE_START_COLOR = -12386427;
   private static final int SPLASH_LINE_END_COLOR = -42920;
   private static final double AIR_INERTIA = 0.99;
   private static final double THROWABLE_WATER_INERTIA = 0.8;
   private static final double ARROW_WATER_INERTIA = 0.6;
   private static final double TRIDENT_WATER_INERTIA = 0.99;
   private static final double THROWABLE_GRAVITY = 0.03;
   private static final double POTION_GRAVITY = 0.05;
   private static final double EXPERIENCE_BOTTLE_GRAVITY = 0.07;
   private static final double ARROW_GRAVITY = 0.05;

   public TrajectoriesFeature() {
      super("Trajectories", "Predicts projectile paths and impact points", FeatureCategory.VISUAL, -1);
   }

   public static TrajectoriesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(TrajectoriesFeature.class);
   }

   @EventTarget
   public void on2DRender(Render2DEvent event) {
      if (!this.pendingScreenLines.isEmpty() || !this.points.isEmpty()) {
         float unit = ScaleUtil.toGuiPixels(1.6F, mc.method_22683().method_4495());
         float centerX = event.getGuiGraphicsExtractor().method_51421() * 0.5F;
         float centerY = event.getGuiGraphicsExtractor().method_51443() * 0.5F;
         float connectorThickness = Math.max(0.5F, 1.35F * unit);

         for (TrajectoriesFeature.ScreenLine line : this.pendingScreenLines) {
            this.drawScreenLine(event, centerX, centerY, line.x(), line.y(), connectorThickness, line.color());
         }

         for (TrajectoriesFeature.ImpactPoint point : this.points) {
            TrajectoriesFeature.ScreenPoint screen = this.projectToScreen(point.pos());
            if (screen != null) {
               this.renderImpactTag(event, point, screen.x(), screen.y(), unit);
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   public void renderWorld() {
      this.pendingScreenLines.clear();
      this.points.clear();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_243 oldPlayerPos = mc.field_1724.method_61411();
         double dx = mc.field_1724.method_23317() - oldPlayerPos.field_1352;
         double dy = mc.field_1724.method_23318() - oldPlayerPos.field_1351;
         double dz = mc.field_1724.method_23321() - oldPlayerPos.field_1350;
         if (dx * dx + dy * dy + dz * dz > 36.0) {
            this.tpSkipTicks = 2;
         }

         if (this.tpSkipTicks > 0) {
            this.tpSkipTicks--;
            this.pendingScreenLines.clear();
         } else {
            List<WorldMeshRenderer.Line> lines = new ArrayList<>();
            List<WorldMeshRenderer.Ring> rings = new ArrayList<>();
            List<WorldMeshRenderer.PlaneRect> planeRects = new ArrayList<>();
            if (this.predictTrajectory.isSelected("Self")) {
               this.drawPredictionInHand(lines, rings, planeRects);
            }

            if (this.predictTrajectory.isSelected("Players")) {
               this.drawOtherPlayersPrediction(lines, rings, planeRects);
            }

            for (class_1297 entity : mc.field_1687.method_18112()) {
               if ((entity instanceof class_1676 || entity instanceof class_1542) && !this.isStationary(entity)) {
                  class_243 motion = entity.method_18798();
                  float partialTicks = mc.method_61966().method_60637(false);
                  double startX = class_3532.method_16436(partialTicks, entity.field_6014, entity.method_23317());
                  double startY = class_3532.method_16436(partialTicks, entity.field_6036, entity.method_23318());
                  double startZ = class_3532.method_16436(partialTicks, entity.field_5969, entity.method_23321());
                  class_243 pos = new class_243(startX, startY, startZ);

                  for (int tick = 0; tick < 300; tick++) {
                     class_243 prevPos = pos;
                     TrajectoriesFeature.TrajectoryStep step;
                     if (entity instanceof class_1676 projectile) {
                        step = this.simulateProjectileStep(projectile, pos, motion);
                     } else {
                        class_243 nextPos = pos.method_1019(motion);
                        class_243 nextMotion = this.calculateMotion(entity, prevPos, motion);
                        class_239 hit = this.raycastBlock(prevPos, nextPos, entity);
                        step = new TrajectoriesFeature.TrajectoryStep(
                           hit.method_17783() != class_240.field_1333 ? hit.method_17784() : nextPos,
                           nextMotion,
                           hit.method_17783() != class_240.field_1333 ? hit : null
                        );
                     }

                     class_243 renderEnd = step.hitResult() != null ? step.hitResult().method_17784() : step.nextPos();
                     float alpha = class_3532.method_15363(tick / 7.0F, 0.0F, 1.0F);
                     lines.add(new WorldMeshRenderer.Line(prevPos, renderEnd, ColorUtil.applyAlpha(this.animatedAccentColor(tick), alpha)));
                     if (step.hitResult() != null || step.nextPos().field_1351 < -128.0) {
                        this.registerImpact(entity, renderEnd, tick);
                        break;
                     }

                     pos = step.nextPos();
                     motion = step.nextMotion();
                  }
               }
            }

            WorldMeshRenderer.render(new WorldMeshRenderer.WorldMesh(lines, rings, planeRects));
         }
      } else {
         this.clear();
      }
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   private void drawPredictionInHand(List<WorldMeshRenderer.Line> lines, List<WorldMeshRenderer.Ring> rings, List<WorldMeshRenderer.PlaneRect> planeRects) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.drawPredictionForPlayer(mc.field_1724, true, lines, rings, planeRects);
      }
   }

   private void drawOtherPlayersPrediction(List<WorldMeshRenderer.Line> lines, List<WorldMeshRenderer.Ring> rings, List<WorldMeshRenderer.PlaneRect> planeRects) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724 && player.method_5805() && !player.method_31481()) {
               this.drawPredictionForPlayer(player, false, lines, rings, planeRects);
            }
         }
      }
   }

   private void drawPredictionForPlayer(
      class_1657 player,
      boolean screenAnchored,
      List<WorldMeshRenderer.Line> lines,
      List<WorldMeshRenderer.Ring> rings,
      List<WorldMeshRenderer.PlaneRect> planeRects
   ) {
      if (mc.field_1687 != null && player != null) {
         class_1799 activeStack = player.method_6030();
         class_1799[] stacks = new class_1799[]{player.method_6047(), player.method_6079()};

         for (class_1799 stack : stacks) {
            if (!stack.method_7960()) {
               float partialTicks = mc.method_61966().method_60637(false);
               float pitch = class_3532.method_16439(partialTicks, player.field_6004, player.method_36455());
               float yaw = class_3532.method_16439(partialTicks, player.field_5982, player.method_36454());
               List<TrajectoriesFeature.Prediction> predictions = new ArrayList<>();
               class_1792 item = stack.method_7909();
               if (item instanceof class_1779) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_1683(mc.field_1687, player, stack), 0.7, pitch, yaw, -20.0F, screenAnchored)
                  );
               } else if (item instanceof class_1828) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_10691(mc.field_1687, player, stack), 0.5, pitch, yaw, -20.0F, screenAnchored)
                  );
               } else if (item instanceof class_1803) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_10690(mc.field_1687, player, stack), 0.5, pitch, yaw, -20.0F, screenAnchored)
                  );
               } else if (item instanceof class_1835 && !activeStack.method_7960() && activeStack.method_7909() == item && player.method_6048() >= 10) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_1685(mc.field_1687, player, stack), 2.5, pitch, yaw, 0.0F, screenAnchored)
                  );
               } else if (item instanceof class_1823) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_1680(mc.field_1687, player, stack), 1.5, pitch, yaw, 0.0F, screenAnchored)
                  );
               } else if (item instanceof class_1771) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_1681(mc.field_1687, player, stack), 1.5, pitch, yaw, 0.0F, screenAnchored)
                  );
               } else if (item instanceof class_1776) {
                  this.addPrediction(
                     predictions, this.checkTrajectory(player, new class_1684(mc.field_1687, player, stack), 1.5, pitch, yaw, 0.0F, screenAnchored)
                  );
               } else if (item instanceof class_1753 && !activeStack.method_7960() && activeStack.method_7909() == item && player.method_6115()) {
                  float power = class_1753.method_7722(player.method_6048()) * 3.0F;
                  this.addPrediction(predictions, this.checkTrajectory(player, this.newArrow(player, stack), power, pitch, yaw, 0.0F, screenAnchored));
               } else if (item instanceof class_1764 && class_1764.method_7781(stack)) {
                  class_9278 charged = (class_9278)stack.method_58694(class_9334.field_49649);
                  if (charged != null && !charged.method_57442()) {
                     double velocity = charged.method_57438(class_1802.field_8639) ? 1.6 : 3.15;
                     this.addPrediction(
                        predictions,
                        this.checkTrajectory(
                           player, this.buildCrossbowDirection(player, partialTicks, 0.0F), this.newArrow(player, stack), velocity, screenAnchored
                        )
                     );
                     if (charged.method_57437().size() > 2) {
                        this.addPrediction(
                           predictions,
                           this.checkTrajectory(
                              player, this.buildCrossbowDirection(player, partialTicks, -10.0F), this.newArrow(player, stack), velocity, screenAnchored
                           )
                        );
                        this.addPrediction(
                           predictions,
                           this.checkTrajectory(
                              player, this.buildCrossbowDirection(player, partialTicks, 10.0F), this.newArrow(player, stack), velocity, screenAnchored
                           )
                        );
                     }
                  }
               }

               for (TrajectoriesFeature.Prediction prediction : predictions) {
                  if (screenAnchored) {
                     TrajectoriesFeature.ScreenPoint connectorEnd = this.projectToScreen(prediction.screenConnectorEnd());
                     if (connectorEnd != null) {
                        this.pendingScreenLines.add(new TrajectoriesFeature.ScreenLine(connectorEnd.x(), connectorEnd.y(), this.animatedAccentColor(0)));
                     }
                  }

                  lines.addAll(prediction.lines());
                  if (prediction.result() != null) {
                     this.addImpactMarker(lines, rings, planeRects, prediction.projectile(), prediction.result());
                  }
               }
            }
         }
      }
   }

   private class_1667 newArrow(class_1309 shooter, class_1799 weapon) {
      return new class_1667(
         mc.field_1687, shooter.method_23317(), shooter.method_23320() - 0.1, shooter.method_23321(), new class_1799(class_1802.field_8107), weapon
      );
   }

   private TrajectoriesFeature.Prediction checkTrajectory(
      class_1309 shooter, class_1676 entity, double velocity, float pitch, float yaw, float angleOffset, boolean screenAnchored
   ) {
      return this.checkTrajectory(shooter, this.buildShootFromRotationDirection(pitch, yaw, angleOffset), entity, velocity, screenAnchored);
   }

   private TrajectoriesFeature.Prediction checkTrajectory(class_1309 shooter, class_243 lookVec, class_1676 entity, double velocity, boolean screenAnchored) {
      if (shooter == null) {
         return new TrajectoriesFeature.Prediction(entity, null, List.of(), null);
      }

      float partialTicks = mc.method_61966().method_60637(false);
      double startX = class_3532.method_16436(partialTicks, shooter.field_6014, shooter.method_23317());
      double startY = class_3532.method_16436(partialTicks, shooter.field_6036, shooter.method_23318()) + shooter.method_5751();
      double startZ = class_3532.method_16436(partialTicks, shooter.field_5969, shooter.method_23321());
      class_243 startPos = new class_243(startX, startY - 0.1, startZ);
      entity.method_5814(startPos.field_1352, startPos.field_1351, startPos.field_1350);
      class_243 motion;
      if (entity instanceof class_1665 arrow && arrow.method_59958() != null && arrow.method_59958().method_7909() == class_1802.field_8399) {
         motion = lookVec.method_1029().method_1021(velocity);
      } else {
         motion = this.buildShootFromRotationMotion(shooter, lookVec, velocity, true);
      }

      return this.traceTrajectory(startPos, motion, entity, screenAnchored);
   }

   private TrajectoriesFeature.Prediction traceTrajectory(class_243 start, class_243 startMotion, class_1676 entity, boolean screenAnchored) {
      List<WorldMeshRenderer.Line> lines = new ArrayList<>();
      class_243 screenConnectorEnd = null;
      class_243 pos = start;
      class_243 motion = startMotion;

      for (int tick = 0; tick < 300; tick++) {
         TrajectoriesFeature.TrajectoryStep step = this.simulateProjectileStep(entity, pos, motion);
         class_243 renderEnd = step.hitResult() != null ? step.hitResult().method_17784() : step.nextPos();
         float alpha = class_3532.method_15363(tick / 7.0F, 0.0F, 1.0F);
         if (tick == 0 && screenAnchored) {
            screenConnectorEnd = renderEnd;
         } else {
            lines.add(new WorldMeshRenderer.Line(pos, renderEnd, ColorUtil.applyAlpha(this.animatedAccentColor(tick), alpha)));
         }

         if (step.hitResult() != null) {
            return new TrajectoriesFeature.Prediction(entity, step.hitResult(), lines, screenConnectorEnd);
         }

         if (step.nextPos().field_1351 < -128.0) {
            break;
         }

         pos = step.nextPos();
         motion = step.nextMotion();
      }

      return new TrajectoriesFeature.Prediction(entity, null, lines, screenConnectorEnd);
   }

   private TrajectoriesFeature.TrajectoryStep simulateProjectileStep(class_1676 projectile, class_243 pos, class_243 motion) {
      return projectile instanceof class_1665 arrow ? this.simulateArrowStep(arrow, pos, motion) : this.simulateThrowableStep(projectile, pos, motion);
   }

   private TrajectoriesFeature.TrajectoryStep simulateThrowableStep(class_1676 projectile, class_243 pos, class_243 motion) {
      class_243 nextMotion = motion.method_1031(0.0, -this.projectileGravity(projectile), 0.0).method_1021(this.isInWater(pos) ? 0.8 : 0.99);
      class_239 hit = this.raycastProjectile(pos, nextMotion, projectile);
      return new TrajectoriesFeature.TrajectoryStep(hit != null ? hit.method_17784() : pos.method_1019(nextMotion), nextMotion, hit);
   }

   private TrajectoriesFeature.TrajectoryStep simulateArrowStep(class_1665 arrow, class_243 pos, class_243 motion) {
      boolean inWater = this.isInWater(pos);
      class_243 moveDelta = inWater ? motion.method_1021(arrow instanceof class_1685 ? 0.99 : 0.6) : motion;
      class_239 hit = this.raycastProjectile(pos, moveDelta, arrow);
      class_243 nextPos = hit != null ? hit.method_17784() : pos.method_1019(moveDelta);
      class_243 nextMotion = inWater ? moveDelta.method_1031(0.0, -0.05, 0.0) : moveDelta.method_1021(0.99).method_1031(0.0, -0.05, 0.0);
      return new TrajectoriesFeature.TrajectoryStep(nextPos, nextMotion, hit);
   }

   private class_239 raycastProjectile(class_243 start, class_243 motion, class_1676 projectile) {
      if (mc.field_1687 == null) {
         return null;
      }

      class_243 end = start.method_1019(motion);
      class_239 hit = mc.field_1687.method_61717(new class_3959(start, end, class_3960.field_17558, class_242.field_1348, projectile));
      if (hit.method_17783() != class_240.field_1333) {
         end = hit.method_17784();
      }

      class_238 boxAtStart = projectile.method_5829().method_997(start.method_1020(projectile.method_73189()));
      class_3966 entityHit = class_1675.method_18077(
         mc.field_1687,
         projectile,
         start,
         end,
         boxAtStart.method_18804(motion).method_1014(1.0),
         candidate -> this.canHitProjectileTarget(projectile, candidate)
      );
      if (entityHit != null) {
         hit = entityHit;
      }

      return hit.method_17783() != class_240.field_1333 ? hit : null;
   }

   private boolean canHitProjectileTarget(class_1676 projectile, class_1297 candidate) {
      if (!candidate.method_49108()) {
         return false;
      } else {
         class_1297 owner = projectile.method_24921();
         if (owner == null) {
            return true;
         } else {
            return candidate != owner && !owner.method_5794(candidate)
               ? !(projectile instanceof class_1665 && owner instanceof class_1657 ownerPlayer)
                  || !(candidate instanceof class_1657 targetPlayer && !ownerPlayer.method_7256(targetPlayer))
               : false;
         }
      }
   }

   private double projectileGravity(class_1676 projectile) {
      if (projectile instanceof class_1665) {
         return 0.05;
      } else if (projectile instanceof class_1683) {
         return 0.07;
      } else if (projectile instanceof class_1686) {
         return 0.05;
      } else {
         return projectile instanceof class_3857 ? 0.03 : 0.03;
      }
   }

   private boolean isInWater(class_243 pos) {
      return mc.field_1687 != null && mc.field_1687.method_8316(class_2338.method_49638(pos)).method_15767(class_3486.field_15517);
   }

   private class_243 buildShootFromRotationMotion(class_1309 shooter, class_243 direction, double velocity, boolean addShooterMovement) {
      class_243 motion = direction.method_1029().method_1021(velocity);
      if (addShooterMovement) {
         class_243 knownMovement = shooter.method_60478();
         motion = motion.method_1031(knownMovement.field_1352, shooter.method_24828() ? 0.0 : knownMovement.field_1351, knownMovement.field_1350);
      }

      return motion;
   }

   private class_243 buildShootFromRotationDirection(float pitch, float yaw, float angleOffset) {
      double pitchRad = pitch * (Math.PI / 180.0);
      double yawRad = yaw * (Math.PI / 180.0);
      double x = -Math.sin(yawRad) * Math.cos(pitchRad);
      double y = -Math.sin((pitch + angleOffset) * (Math.PI / 180.0));
      double z = Math.cos(yawRad) * Math.cos(pitchRad);
      return new class_243(x, y, z);
   }

   private class_243 buildCrossbowDirection(class_1309 shooter, float partialTicks, float angle) {
      if (angle == 0.0F) {
         return shooter.method_5828(partialTicks);
      }

      class_243 up = shooter.method_18864(partialTicks);
      Quaternionf rotation = new Quaternionf().setAngleAxis(angle * (Math.PI / 180.0), up.field_1352, up.field_1351, up.field_1350);
      Vector3f rotated = shooter.method_5828(partialTicks).method_46409().rotate(rotation);
      return new class_243(rotated.x, rotated.y, rotated.z);
   }

   private class_243 calculateMotion(class_1297 entity, class_243 prevPos, class_243 motion) {
      boolean water = mc.field_1687 != null && mc.field_1687.method_8316(class_2338.method_49638(prevPos)).method_15767(class_3486.field_15517);
      double inertia;
      double gravity;
      if (entity instanceof class_1685) {
         inertia = 0.99;
         gravity = 0.05;
      } else if (entity instanceof class_1665) {
         inertia = water ? 0.6 : 0.99;
         gravity = 0.05;
      } else if (entity instanceof class_1683) {
         inertia = water ? 0.8 : 0.99;
         gravity = 0.07;
      } else if (entity instanceof class_1686) {
         inertia = water ? 0.8 : 0.99;
         gravity = 0.05;
      } else if (entity instanceof class_3857) {
         inertia = water ? 0.8 : 0.99;
         gravity = 0.03;
      } else if (entity instanceof class_1542) {
         inertia = water ? 0.8 : 0.98;
         gravity = 0.04;
      } else {
         inertia = 0.99;
         gravity = 0.03;
      }

      return motion.method_1021(inertia).method_1031(0.0, -gravity, 0.0);
   }

   private void addImpactMarker(
      List<WorldMeshRenderer.Line> lines,
      List<WorldMeshRenderer.Ring> rings,
      List<WorldMeshRenderer.PlaneRect> planeRects,
      class_1676 projectile,
      class_239 result
   ) {
      class_2350 direction = this.getDirection(result);
      int color = result.method_17783() == class_240.field_1331 ? -48060 : this.animatedAccentColor(0);
      class_243 center = result.method_17784().method_1031(direction.method_10148() * 0.01, direction.method_10164() * 0.01, direction.method_10165() * 0.01);
      double width = 0.12;
      class_243 u;
      class_243 v;
      switch (direction.method_10166()) {
         case field_11048:
            u = new class_243(0.0, 1.0, 0.0);
            v = new class_243(0.0, 0.0, 1.0);
            break;
         case field_11052:
            u = new class_243(1.0, 0.0, 0.0);
            v = new class_243(0.0, 0.0, 1.0);
            break;
         case field_11051:
            u = new class_243(1.0, 0.0, 0.0);
            v = new class_243(0.0, 1.0, 0.0);
            break;
         default:
            u = new class_243(1.0, 0.0, 0.0);
            v = new class_243(0.0, 0.0, 1.0);
      }

      rings.add(new WorldMeshRenderer.Ring(center, u, v, width, 0.004, color, 180));
      double crossRadius = width * 0.72;
      planeRects.add(new WorldMeshRenderer.PlaneRect(center, u, v, crossRadius, 0.003, color));
      planeRects.add(new WorldMeshRenderer.PlaneRect(center, v, u, crossRadius, 0.003, color));
      if (result instanceof class_3966 entityHit) {
         this.addEntityHitBox(lines, planeRects, entityHit.method_17782());
      }

      Double areaRadius = this.potionAreaRadius(projectile);
      if (areaRadius != null) {
         class_243 areaCenter = this.resolvePotionAreaCenter(projectile, result);
         int areaColor = this.animatedAccentColor(0);
         rings.add(
            new WorldMeshRenderer.Ring(
               areaCenter, new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), areaRadius, 0.018, ColorUtil.applyAlpha(areaColor, 0.55F), 220
            )
         );
         if (areaCenter.method_1025(result.method_17784()) > 1.0E-4) {
            lines.add(new WorldMeshRenderer.Line(result.method_17784(), areaCenter, ColorUtil.applyAlpha(areaColor, 0.67F)));
         }

         if (projectile instanceof class_10691 splashPotion) {
            this.addSplashExposureLines(lines, splashPotion, result, areaCenter);
         }
      }
   }

   private void registerImpact(class_1297 entity, class_243 pos, int ticks) {
      class_1799 stack = this.iconFor(entity);
      String ownerName = entity instanceof class_1676 projectile ? this.ownerName(projectile.method_24921()) : null;
      List<class_1293> effects = new ArrayList<>();
      class_1844 potionContents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (potionContents != null) {
         potionContents.method_57397().forEach(effects::add);
      }

      this.points.add(new TrajectoriesFeature.ImpactPoint(stack.method_7972(), pos, ticks, entity.field_6012, ownerName, List.copyOf(effects)));
   }

   private String ownerName(class_1297 owner) {
      return owner instanceof class_1309 living ? living.method_5477().getString() : null;
   }

   private class_1799 iconFor(class_1297 entity) {
      if (entity instanceof class_1542 itemEntity) {
         return itemEntity.method_6983();
      } else {
         if (entity instanceof class_3856 supplier) {
            class_1799 supplied = supplier.method_7495();
            if (!supplied.method_7960()) {
               return supplied;
            }
         }

         if (entity instanceof class_1665 arrow) {
            class_1799 pickup = arrow.method_54759();
            if (!pickup.method_7960()) {
               return pickup;
            }
         }

         class_1799 picked = entity.method_31480();
         return picked != null && !picked.method_7960() ? picked : new class_1799(this.fallbackIcon(entity));
      }
   }

   private class_1792 fallbackIcon(class_1297 entity) {
      if (entity instanceof class_1685) {
         return class_1802.field_8547;
      } else if (entity instanceof class_1679) {
         return class_1802.field_8236;
      } else if (entity instanceof class_1665) {
         return class_1802.field_8107;
      } else if (entity instanceof class_9236) {
         return class_1802.field_49098;
      } else if (entity instanceof class_1670) {
         return class_1802.field_8613;
      } else if (entity instanceof class_1687) {
         return class_1802.field_8791;
      } else if (entity instanceof class_3855) {
         return class_1802.field_8814;
      } else if (entity instanceof class_1678) {
         return class_1802.field_8815;
      } else if (entity instanceof class_1536) {
         return class_1802.field_8378;
      } else if (entity instanceof class_1671) {
         return class_1802.field_8639;
      } else if (entity instanceof class_1672) {
         return class_1802.field_8449;
      } else {
         return entity instanceof class_1673 ? class_1802.field_8777 : class_1802.field_8543;
      }
   }

   private void renderImpactTag(Render2DEvent event, TrajectoriesFeature.ImpactPoint point, float screenX, float screenY, float unit) {
      float width = 36.0F * unit;
      float card = 36.0F * unit;
      float height = 48.0F * unit;
      float gap = 4.0F * unit;
      float radius = 10.0F * unit;
      float border = Math.max(1.0F, 1.2F * unit);
      float icon = 20.0F * unit;
      float textSize = 8.0F * unit;
      float textRow = 8.0F * unit;
      float x = screenX - width * 0.5F;
      float y = screenY - height * 0.5F;
      int totalTicks = point.ticks() + point.entityAge();
      float progress = totalTicks > 0 ? class_3532.method_15363((float)point.ticks() / totalTicks, 0.0F, 1.0F) : 0.0F;
      int accent = Theme.getAccent();
      Render2DUtil.rect(x, y, card, card).color(Theme.Colors.BACKGROUND_PRIMARY_50).radius(radius).blur(8.0F * unit).draw();
      this.drawRoundedProgressBorder(event, x, y, card, radius, border, progress, accent);
      float iconOffset = (card - icon) * 0.5F;
      this.drawScaledItem(event, point.stack(), x + iconOffset, y + iconOffset, icon);
      String time = this.formatSeconds(point.ticks());
      float textCenterX = x + width * 0.5F;
      float textCenterY = y + card + gap + textRow * 0.5F;
      Render2DUtil.text(textCenterX, UiFonts.sfProDisplay().centeredTextY(textCenterY, textSize), textSize, time)
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.CENTER)
         .color(-1)
         .draw();
   }

   private void drawScaledItem(Render2DEvent event, class_1799 stack, float x, float y, float size) {
      if (!stack.method_7960()) {
         Render2DUtil.flush();
         double guiScale = mc.method_22683().method_4495();
         float scale = size / 16.0F;
         float itemX = (float)(Math.round(x * guiScale) / guiScale);
         float itemY = (float)(Math.round(y * guiScale) / guiScale);
         Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
         pose.pushMatrix();
         pose.translate(itemX, itemY);
         pose.scale(scale);
         event.getGuiGraphicsExtractor().method_51427(stack, 0, 0);
         pose.popMatrix();
      }
   }

   private void drawRoundedProgressBorder(Render2DEvent event, float x, float y, float size, float radius, float thickness, float progress, int color) {
      progress = class_3532.method_15363(progress, 0.0F, 1.0F);
      if (!(progress <= 0.001F) && !(thickness <= 0.0F)) {
         float straight = Math.max(0.0F, size - radius * 2.0F);
         float arc = (float)((Math.PI / 2) * radius);
         float perimeter = straight * 4.0F + arc * 4.0F;
         float target = perimeter * progress;
         float half = thickness * 0.5F;
         int samples = Math.max(48, Math.round(perimeter / 1.5F));
         float traveled = 0.0F;
         float prevX = x + size * 0.5F;
         float prevY = y;

         for (int i = 1; i <= samples; i++) {
            float distance = perimeter * ((float)i / samples);
            float[] point = pointOnRoundedRect(x, y, size, radius, straight, arc, distance);
            float seg = distance - traveled;
            if (traveled < target) {
               float drawLen = Math.min(seg, target - traveled);
               float t = drawLen / seg;
               float endX = prevX + (point[0] - prevX) * t;
               float endY = prevY + (point[1] - prevY) * t;
               this.strokeSegment(event, prevX, prevY, endX, endY, half, color);
            }

            traveled = distance;
            prevX = point[0];
            prevY = point[1];
            if (traveled >= target) {
               break;
            }
         }
      }
   }

   private static float[] pointOnRoundedRect(float x, float y, float size, float radius, float straight, float arc, float distance) {
      float cursor = distance;
      float halfTop = straight * 0.5F;
      if (cursor <= halfTop) {
         return new float[]{x + size * 0.5F + cursor, y};
      } else {
         cursor -= halfTop;
         if (cursor <= arc) {
            float angle = (float)((-Math.PI / 2) + cursor / radius);
            return new float[]{x + size - radius + (float)Math.cos(angle) * radius, y + radius + (float)Math.sin(angle) * radius};
         } else {
            cursor -= arc;
            if (cursor <= straight) {
               return new float[]{x + size, y + radius + cursor};
            } else {
               cursor -= straight;
               if (cursor <= arc) {
                  float angle = cursor / radius;
                  return new float[]{x + size - radius + (float)Math.cos(angle) * radius, y + size - radius + (float)Math.sin(angle) * radius};
               } else {
                  cursor -= arc;
                  if (cursor <= straight) {
                     return new float[]{x + size - radius - cursor, y + size};
                  } else {
                     cursor -= straight;
                     if (cursor <= arc) {
                        float angle = (float)((Math.PI / 2) + cursor / radius);
                        return new float[]{x + radius + (float)Math.cos(angle) * radius, y + size - radius + (float)Math.sin(angle) * radius};
                     } else {
                        cursor -= arc;
                        if (cursor <= straight) {
                           return new float[]{x, y + size - radius - cursor};
                        } else {
                           cursor -= straight;
                           if (cursor <= arc) {
                              float angle = (float)(Math.PI + cursor / radius);
                              return new float[]{x + radius + (float)Math.cos(angle) * radius, y + radius + (float)Math.sin(angle) * radius};
                           } else {
                              cursor -= arc;
                              return new float[]{x + radius + Math.min(cursor, halfTop), y};
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void strokeSegment(Render2DEvent event, float x1, float y1, float x2, float y2, float halfThickness, int color) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      if (length < 0.001F) {
         Render2DUtil.rect(x1 - halfThickness, y1 - halfThickness, halfThickness * 2.0F, halfThickness * 2.0F).color(color).draw();
      } else {
         Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
         pose.pushMatrix();
         pose.translate(x1, y1);
         pose.rotate((float)Math.atan2(dy, dx));
         float overlap = halfThickness * 0.75F;
         Render2DUtil.rect(-overlap, -halfThickness, length + overlap * 2.0F, halfThickness * 2.0F).color(color).draw();
         pose.popMatrix();
      }
   }

   private void addEntityHitBox(List<WorldMeshRenderer.Line> lines, List<WorldMeshRenderer.PlaneRect> planeRects, class_1297 entity) {
      class_238 box = entity.method_5829().method_1014(0.028);
      class_243 center = new class_243(
         (box.field_1323 + box.field_1320) * 0.5, (box.field_1322 + box.field_1325) * 0.5, (box.field_1321 + box.field_1324) * 0.5
      );
      double hx = box.method_17939() * 0.5;
      double hy = box.method_17940() * 0.5;
      double hz = box.method_17941() * 0.5;
      int fillColor = ColorUtil.applyAlpha(this.animatedAccentColor(22), 0.09F);
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(box.field_1323, center.field_1351, center.field_1350), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, 1.0), hy, hz, fillColor
         )
      );
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(box.field_1320, center.field_1351, center.field_1350), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, 1.0), hy, hz, fillColor
         )
      );
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(center.field_1352, box.field_1322, center.field_1350), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), hx, hz, fillColor
         )
      );
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(center.field_1352, box.field_1325, center.field_1350), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), hx, hz, fillColor
         )
      );
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(center.field_1352, center.field_1351, box.field_1321), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), hx, hy, fillColor
         )
      );
      planeRects.add(
         new WorldMeshRenderer.PlaneRect(
            new class_243(center.field_1352, center.field_1351, box.field_1324), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), hx, hy, fillColor
         )
      );
      class_243[] corners = new class_243[]{
         new class_243(box.field_1323, box.field_1322, box.field_1321),
         new class_243(box.field_1323, box.field_1322, box.field_1324),
         new class_243(box.field_1323, box.field_1325, box.field_1321),
         new class_243(box.field_1323, box.field_1325, box.field_1324),
         new class_243(box.field_1320, box.field_1322, box.field_1321),
         new class_243(box.field_1320, box.field_1322, box.field_1324),
         new class_243(box.field_1320, box.field_1325, box.field_1321),
         new class_243(box.field_1320, box.field_1325, box.field_1324)
      };
      int[][] edges = new int[][]{{0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3}, {2, 6}, {3, 7}, {4, 5}, {4, 6}, {5, 7}, {6, 7}};

      for (int i = 0; i < edges.length; i++) {
         int edgeColor = ColorUtil.applyAlpha(this.animatedAccentColor(i * 11), 0.94F);
         lines.add(new WorldMeshRenderer.Line(corners[edges[i][0]], corners[edges[i][1]], edgeColor));
      }
   }

   private void addSplashExposureLines(List<WorldMeshRenderer.Line> lines, class_10691 projectile, class_239 result, class_243 areaCenter) {
      if (mc.field_1687 != null) {
         class_1297 hitEntity = result instanceof class_3966 entityHit ? entityHit.method_17782() : null;
         class_238 affectedBox = new class_238(
            areaCenter.field_1352 - 4.0,
            areaCenter.field_1351 - 2.0,
            areaCenter.field_1350 - 4.0,
            areaCenter.field_1352 + 4.0,
            areaCenter.field_1351 + 2.0,
            areaCenter.field_1350 + 4.0
         );

         for (class_1309 entity : mc.field_1687.method_8390(class_1309.class, affectedBox, candidate -> candidate.method_5805() && !candidate.method_31481())) {
            if (entity != mc.field_1724) {
               class_243 targetPoint = new class_243(entity.method_23317(), areaCenter.field_1351, entity.method_23321());
               class_243 offset = targetPoint.method_1020(areaCenter);
               class_243 horizontal = new class_243(offset.field_1352, 0.0, offset.field_1350);
               double distance = Math.sqrt(horizontal.field_1352 * horizontal.field_1352 + horizontal.field_1350 * horizontal.field_1350);
               if (!(distance > 4.0) && !(distance < 0.001)) {
                  double exposure = entity == hitEntity ? 1.0 : class_3532.method_15350(1.0 - distance / 4.0, 0.0, 1.0);
                  float alpha = (float)(0.38 + 0.54 * exposure);
                  lines.add(new WorldMeshRenderer.Line(areaCenter, targetPoint, ColorUtil.applyAlpha(-12386427, alpha), ColorUtil.applyAlpha(-42920, alpha)));
               }
            }
         }
      }
   }

   private Double potionAreaRadius(class_1676 projectile) {
      if (projectile instanceof class_10691) {
         return 4.0;
      } else {
         return projectile instanceof class_10690 ? 3.0 : null;
      }
   }

   private class_243 resolvePotionAreaCenter(class_1676 projectile, class_239 result) {
      if (mc.field_1687 == null) {
         return result.method_17784();
      }

      class_243 start = result.method_17784().method_1031(0.0, 0.35, 0.0);
      class_243 end = result.method_17784().method_1031(0.0, -2.5, 0.0);
      class_239 groundHit = mc.field_1687.method_61717(new class_3959(start, end, class_3960.field_17558, class_242.field_1348, projectile));
      return groundHit.method_17783() != class_240.field_1333 ? groundHit.method_17784().method_1031(0.0, 0.012, 0.0) : result.method_17784();
   }

   private class_2350 getDirection(class_239 result) {
      if (result instanceof class_3965 blockHit) {
         return blockHit.method_17780();
      } else {
         if (mc.field_1724 == null) {
            return class_2350.field_11036;
         }

         class_243 vec = result.method_17784().method_1020(mc.field_1724.method_33571()).method_1029();
         return class_2350.method_10147((float)vec.field_1352, (float)vec.field_1351, (float)vec.field_1350);
      }
   }

   private boolean isStationary(class_1297 entity) {
      boolean posChange = entity.method_73189().equals(entity.method_61411());
      boolean itemEntityCheck = entity instanceof class_1542
         && (entity.method_24828() || mc.field_1687 != null && mc.field_1687.method_8316(entity.method_24515()).method_15767(class_3486.field_15517));
      return posChange || itemEntityCheck;
   }

   private class_239 raycastBlock(class_243 start, class_243 end, class_1297 entity) {
      return mc.field_1687 == null
         ? class_3965.method_17778(end, class_2350.field_11036, class_2338.method_49638(end))
         : mc.field_1687.method_17742(new class_3959(start, end, class_3960.field_17558, class_242.field_1348, entity));
   }

   private void drawScreenLine(Render2DEvent event, float x1, float y1, float x2, float y2, float thickness, int color) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      if (!(length < 0.001F)) {
         Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
         pose.pushMatrix();
         pose.translate(x1, y1);
         pose.rotate((float)Math.atan2(dy, dx));
         Render2DUtil.rect(0.0F, -thickness * 0.5F, length, thickness).color(color).draw();
         pose.popMatrix();
      }
   }

   private String formatSeconds(int ticks) {
      int seconds = Math.max(0, Math.round(ticks / 20.0F));
      return seconds + "s";
   }

   private TrajectoriesFeature.ScreenPoint projectToScreen(class_243 pos) {
      Render3DUtil.ScreenPoint point = Render3DUtil.projectToScreen(mc, pos);
      return point == null ? null : new TrajectoriesFeature.ScreenPoint(point.x(), point.y());
   }

   private void addPrediction(List<TrajectoriesFeature.Prediction> predictions, TrajectoriesFeature.Prediction prediction) {
      if (prediction != null) {
         predictions.add(prediction);
      }
   }

   private void clear() {
      this.tpSkipTicks = 0;
      this.pendingScreenLines.clear();
      this.points.clear();
   }

   private int animatedAccentColor(int index) {
      return ColorUtil.fade(8, index * 11, Theme.getAccent(), ColorUtil.rgb(101, 228, 255));
   }

   @Environment(EnvType.CLIENT)
   private record ImpactPoint(class_1799 stack, class_243 pos, int ticks, int entityAge, String ownerName, List<class_1293> effects) {
   }

   @Environment(EnvType.CLIENT)
   private record Prediction(class_1676 projectile, class_239 result, List<WorldMeshRenderer.Line> lines, class_243 screenConnectorEnd) {
   }

   @Environment(EnvType.CLIENT)
   private record ScreenLine(float x, float y, int color) {
   }

   @Environment(EnvType.CLIENT)
   private record ScreenPoint(float x, float y) {
   }

   @Environment(EnvType.CLIENT)
   private record TrajectoryStep(class_243 nextPos, class_243 nextMotion, class_239 hitResult) {
   }
}
