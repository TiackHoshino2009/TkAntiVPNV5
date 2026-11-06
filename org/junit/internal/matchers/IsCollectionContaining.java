package org.junit.internal.matchers;

import java.util.ArrayList;
import java.util.Collection;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsEqual;

public class IsCollectionContaining<T> extends TypeSafeMatcher<Iterable<T>> {
   private final Matcher<? extends T> elementMatcher;

   public IsCollectionContaining(Matcher<? extends T> elementMatcher) {
      this.elementMatcher = elementMatcher;
   }

   public boolean matchesSafely(Iterable<T> collection) {
      for(T item : collection) {
         if (this.elementMatcher.matches(item)) {
            return true;
         }
      }

      return false;
   }

   public void describeTo(Description description) {
      description.appendText("a collection containing ").appendDescriptionOf(this.elementMatcher);
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItem(Matcher<? extends T> elementMatcher) {
      return new IsCollectionContaining<Iterable<T>>(elementMatcher);
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItem(T element) {
      return hasItem(IsEqual.equalTo(element));
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItems(Matcher<? extends T>... elementMatchers) {
      Collection<Matcher<? extends Iterable<T>>> all = new ArrayList(elementMatchers.length);

      for(Matcher<? extends T> elementMatcher : elementMatchers) {
         all.add(hasItem(elementMatcher));
      }

      return AllOf.allOf(all);
   }

   @Factory
   public static <T> Matcher<Iterable<T>> hasItems(T... elements) {
      Collection<Matcher<? extends Iterable<T>>> all = new ArrayList(elements.length);

      for(T element : elements) {
         all.add(hasItem(element));
      }

      return AllOf.allOf(all);
   }
}
