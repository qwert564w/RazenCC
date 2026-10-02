package org.ryzen.pve.navigation;
public class BaritoneNavigatorImpl implements Navigator {
    public static final BaritoneNavigatorImpl INSTANCE = new BaritoneNavigatorImpl();
    private BaritoneNavigatorImpl() {}
    public boolean isActive() { return false; }
    @Override public boolean isAvailable() { return false; }
    @Override public void begin(NavigationOptions o) {}
    @Override public void pathTo(net.minecraft.class_2338 p, int r) {}
    @Override public void mine(int r, net.minecraft.class_2248... b) {}
    @Override public void setMineBounds(net.minecraft.class_2338 a, net.minecraft.class_2338 b) {}
    @Override public boolean isPathing() { return false; }
    @Override public boolean isMining() { return false; }
    @Override public java.util.List<net.minecraft.class_2338> miningTargets() { return java.util.List.of(); }
    @Override public java.util.Optional<Double> estimatedTicksToGoal() { return java.util.Optional.empty(); }
    @Override public java.util.Optional<net.minecraft.class_2338> currentGoal() { return java.util.Optional.empty(); }
    @Override public String diagnostics() { return ""; }
    @Override public void cancel() {}
    @Override public void end() {}
}
