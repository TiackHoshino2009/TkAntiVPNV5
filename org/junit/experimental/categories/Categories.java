package org.junit.experimental.categories;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class Categories extends Suite {
   public Categories(Class<?> klass, RunnerBuilder builder) throws InitializationError {
      super(klass, builder);

      try {
         this.filter(new CategoryFilter(this.getIncludedCategory(klass), this.getExcludedCategory(klass)));
      } catch (NoTestsRemainException e) {
         throw new InitializationError(e);
      }

      this.assertNoCategorizedDescendentsOfUncategorizeableParents(this.getDescription());
   }

   private Class<?> getIncludedCategory(Class<?> klass) {
      IncludeCategory annotation = (IncludeCategory)klass.getAnnotation(IncludeCategory.class);
      return annotation == null ? null : annotation.value();
   }

   private Class<?> getExcludedCategory(Class<?> klass) {
      ExcludeCategory annotation = (ExcludeCategory)klass.getAnnotation(ExcludeCategory.class);
      return annotation == null ? null : annotation.value();
   }

   private void assertNoCategorizedDescendentsOfUncategorizeableParents(Description description) throws InitializationError {
      if (!canHaveCategorizedChildren(description)) {
         this.assertNoDescendantsHaveCategoryAnnotations(description);
      }

      for(Description each : description.getChildren()) {
         this.assertNoCategorizedDescendentsOfUncategorizeableParents(each);
      }

   }

   private void assertNoDescendantsHaveCategoryAnnotations(Description description) throws InitializationError {
      for(Description each : description.getChildren()) {
         if (each.getAnnotation(Category.class) != null) {
            throw new InitializationError("Category annotations on Parameterized classes are not supported on individual methods.");
         }

         this.assertNoDescendantsHaveCategoryAnnotations(each);
      }

   }

   private static boolean canHaveCategorizedChildren(Description description) {
      for(Description each : description.getChildren()) {
         if (each.getTestClass() == null) {
            return false;
         }
      }

      return true;
   }

   public static class CategoryFilter extends Filter {
      private final Class<?> fIncluded;
      private final Class<?> fExcluded;

      public static CategoryFilter include(Class<?> categoryType) {
         return new CategoryFilter(categoryType, (Class)null);
      }

      public CategoryFilter(Class<?> includedCategory, Class<?> excludedCategory) {
         this.fIncluded = includedCategory;
         this.fExcluded = excludedCategory;
      }

      public String describe() {
         return "category " + this.fIncluded;
      }

      public boolean shouldRun(Description description) {
         if (this.hasCorrectCategoryAnnotation(description)) {
            return true;
         } else {
            for(Description each : description.getChildren()) {
               if (this.shouldRun(each)) {
                  return true;
               }
            }

            return false;
         }
      }

      private boolean hasCorrectCategoryAnnotation(Description description) {
         List<Class<?>> categories = this.categories(description);
         if (categories.isEmpty()) {
            return this.fIncluded == null;
         } else {
            for(Class<?> each : categories) {
               if (this.fExcluded != null && this.fExcluded.isAssignableFrom(each)) {
                  return false;
               }
            }

            for(Class<?> each : categories) {
               if (this.fIncluded == null || this.fIncluded.isAssignableFrom(each)) {
                  return true;
               }
            }

            return false;
         }
      }

      private List<Class<?>> categories(Description description) {
         ArrayList<Class<?>> categories = new ArrayList();
         categories.addAll(Arrays.asList(this.directCategories(description)));
         categories.addAll(Arrays.asList(this.directCategories(this.parentDescription(description))));
         return categories;
      }

      private Description parentDescription(Description description) {
         Class<?> testClass = description.getTestClass();
         return testClass == null ? null : Description.createSuiteDescription(testClass);
      }

      private Class<?>[] directCategories(Description description) {
         if (description == null) {
            return new Class[0];
         } else {
            Category annotation = (Category)description.getAnnotation(Category.class);
            return annotation == null ? new Class[0] : annotation.value();
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface ExcludeCategory {
      Class<?> value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface IncludeCategory {
      Class<?> value();
   }
}
