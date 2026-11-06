package org.hamcrest.core;

import [Ljava.lang.Object;;
import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class DescribedAs<T> extends BaseMatcher<T> {
   private final String descriptionTemplate;
   private final Matcher<T> matcher;
   private final Object[] values;
   private static final Pattern ARG_PATTERN = Pattern.compile("%([0-9]+)");

   public DescribedAs(String descriptionTemplate, Matcher<T> matcher, Object[] values) {
      this.descriptionTemplate = descriptionTemplate;
      this.matcher = matcher;
      this.values = ((Object;)values).clone();
   }

   public boolean matches(Object o) {
      return this.matcher.matches(o);
   }

   public void describeTo(Description description) {
      java.util.regex.Matcher arg = ARG_PATTERN.matcher(this.descriptionTemplate);

      int textStart;
      for(textStart = 0; arg.find(); textStart = arg.end()) {
         description.appendText(this.descriptionTemplate.substring(textStart, arg.start()));
         int argIndex = Integer.parseInt(arg.group(1));
         description.appendValue(this.values[argIndex]);
      }

      if (textStart < this.descriptionTemplate.length()) {
         description.appendText(this.descriptionTemplate.substring(textStart));
      }

   }

   @Factory
   public static <T> Matcher<T> describedAs(String description, Matcher<T> matcher, Object... values) {
      return new DescribedAs<T>(description, matcher, values);
   }
}
