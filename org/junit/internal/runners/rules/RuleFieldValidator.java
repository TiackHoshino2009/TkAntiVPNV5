package org.junit.internal.runners.rules;

import java.lang.annotation.Annotation;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

public enum RuleFieldValidator {
   CLASS_RULE_VALIDATOR(ClassRule.class, true),
   RULE_VALIDATOR(Rule.class, false);

   private final Class<? extends Annotation> fAnnotation;
   private final boolean fOnlyStaticFields;

   private RuleFieldValidator(Class<? extends Annotation> annotation, boolean onlyStaticFields) {
      this.fAnnotation = annotation;
      this.fOnlyStaticFields = onlyStaticFields;
   }

   public void validate(TestClass target, List<Throwable> errors) {
      for(FrameworkField each : target.getAnnotatedFields(this.fAnnotation)) {
         this.validateField(each, errors);
      }

   }

   private void validateField(FrameworkField field, List<Throwable> errors) {
      this.optionallyValidateStatic(field, errors);
      this.validatePublic(field, errors);
      this.validateTestRuleOrMethodRule(field, errors);
   }

   private void optionallyValidateStatic(FrameworkField field, List<Throwable> errors) {
      if (this.fOnlyStaticFields && !field.isStatic()) {
         this.addError(errors, field, "must be static.");
      }

   }

   private void validatePublic(FrameworkField field, List<Throwable> errors) {
      if (!field.isPublic()) {
         this.addError(errors, field, "must be public.");
      }

   }

   private void validateTestRuleOrMethodRule(FrameworkField field, List<Throwable> errors) {
      if (!this.isMethodRule(field) && !this.isTestRule(field)) {
         this.addError(errors, field, "must implement MethodRule or TestRule.");
      }

   }

   private boolean isTestRule(FrameworkField target) {
      return TestRule.class.isAssignableFrom(target.getType());
   }

   private boolean isMethodRule(FrameworkField target) {
      return MethodRule.class.isAssignableFrom(target.getType());
   }

   private void addError(List<Throwable> errors, FrameworkField field, String suffix) {
      String message = "The @" + this.fAnnotation.getSimpleName() + " '" + field.getName() + "' " + suffix;
      errors.add(new Exception(message));
   }
}
