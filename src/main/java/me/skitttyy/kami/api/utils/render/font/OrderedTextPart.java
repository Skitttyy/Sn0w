package me.skitttyy.kami.api.utils.render.font;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public class OrderedTextPart {
   private final Style style;
   private final StringBuilder text = new StringBuilder();

   public String getText() {
      return this.text.toString();
   }

   public void append(char c) {
      this.text.append(c);
   }

   public static List<OrderedTextPart> getParts(OrderedText orderedText) {
      List<OrderedTextPart> parts = new ArrayList();
      AtomicReference<Style> styleRef = new AtomicReference((Object)null);
      orderedText.accept((charIndex, style, codePoint) -> {
         char c = (char)codePoint;
         if (styleRef.get() == null || !((Style)styleRef.get()).equals(style)) {
            styleRef.set(style);
            parts.add(new OrderedTextPart(style));
         }

         ((OrderedTextPart)parts.get(parts.size() - 1)).append(c);
         return true;
      });
      return parts;
   }


   public Style getStyle() {
      return this.style;
   }


   public OrderedTextPart(Style style) {
      this.style = style;
   }
}