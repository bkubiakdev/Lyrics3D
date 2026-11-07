package dev.bkubiak.lyrics3d.ui.kit.port.data;

import dev.bkubiak.lyrics3d.ui.kit.port.module.CategoryType;

public final class ChoiceOption {
   private final String text;
   private final CategoryType categoryType;

   public ChoiceOption(String text2, CategoryType categoryType2) {
      this.text = text2;
      this.categoryType = categoryType2;
   }

   public ChoiceOption(String text) {
      this(text, null);
   }

   public CategoryType getCategoryType() {
      return this.categoryType;
   }

   public String getText() {
      return this.text;
   }
}
