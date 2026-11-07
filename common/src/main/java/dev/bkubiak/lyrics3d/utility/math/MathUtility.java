package dev.bkubiak.lyrics3d.utility.math;

public final class MathUtility {
   private static final double[] TRIG_TABLE = new double[65536];

   private MathUtility() {
   }

   public static double sin(double radians) {
      int index = (int)(radians * 10430.378350470453) & 65535;
      return TRIG_TABLE[index];
   }

   public static double cos(double radians) {
      int index = (int)(radians * 10430.378350470453 + 16384.0) & 65535;
      return TRIG_TABLE[index];
   }

   public static float random(double min, double max) {
      return (float)(min + (max - min) * Math.random());
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      return (float)(oldValue + (newValue - oldValue) * interpolationValue);
   }

   static {
      for (int i = 0; i < 65536; i++) {
         TRIG_TABLE[i] = Math.sin(i * (Math.PI * 2) / 65536.0);
      }
   }
}
