package dev.bkubiak.lyrics3d.systems.localization;

import lombok.Generated;

public enum Language {
   EN_US("en_us"),
   PL_PL("pl_pl");

   private final String code;

   Language(String code) {
      this.code = code;
   }

   @Generated
   public String getCode() {
      return this.code;
   }
}
