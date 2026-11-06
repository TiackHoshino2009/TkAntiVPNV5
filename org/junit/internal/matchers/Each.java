package org.junit.internal.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Each {
   public static <T> Matcher<Iterable<T>> each(final Matcher<T> individual) {
      final Matcher<Iterable<T>> allItemsAre = CoreMatchers.not(IsCollectionContaining.hasItem(CoreMatchers.not(individual)));
      return new BaseMatcher<Iterable<T>>() {
         public boolean matches(Object item) {
            return allItemsAre.matches(item);
         }

         public void describeTo(Description description) {
            description.appendText("each ");
            individual.describeTo(description);
         }
      };
   }
}
