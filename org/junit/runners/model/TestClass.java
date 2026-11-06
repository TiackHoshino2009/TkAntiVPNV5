package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

public class TestClass {
   private final Class<?> fClass;
   private Map<Class<?>, List<FrameworkMethod>> fMethodsForAnnotations = new HashMap();
   private Map<Class<?>, List<FrameworkField>> fFieldsForAnnotations = new HashMap();

   public TestClass(Class<?> klass) {
      this.fClass = klass;
      if (klass != null && klass.getConstructors().length > 1) {
         throw new IllegalArgumentException("Test class can only have one constructor");
      } else {
         for(Class<?> eachClass : this.getSuperClasses(this.fClass)) {
            for(Method eachMethod : eachClass.getDeclaredMethods()) {
               this.addToAnnotationLists(new FrameworkMethod(eachMethod), this.fMethodsForAnnotations);
            }

            for(Field eachField : eachClass.getDeclaredFields()) {
               this.addToAnnotationLists(new FrameworkField(eachField), this.fFieldsForAnnotations);
            }
         }

      }
   }

   private <T extends FrameworkMember<T>> void addToAnnotationLists(T member, Map<Class<?>, List<T>> map) {
      for(Annotation each : member.getAnnotations()) {
         Class<? extends Annotation> type = each.annotationType();
         List<T> members = this.<T>getAnnotatedMembers(map, type);
         if (member.isShadowedBy(members)) {
            return;
         }

         if (this.runsTopToBottom(type)) {
            members.add(0, member);
         } else {
            members.add(member);
         }
      }

   }

   public List<FrameworkMethod> getAnnotatedMethods(Class<? extends Annotation> annotationClass) {
      return this.<FrameworkMethod>getAnnotatedMembers(this.fMethodsForAnnotations, annotationClass);
   }

   public List<FrameworkField> getAnnotatedFields(Class<? extends Annotation> annotationClass) {
      return this.<FrameworkField>getAnnotatedMembers(this.fFieldsForAnnotations, annotationClass);
   }

   private <T> List<T> getAnnotatedMembers(Map<Class<?>, List<T>> map, Class<? extends Annotation> type) {
      if (!map.containsKey(type)) {
         map.put(type, new ArrayList());
      }

      return (List)map.get(type);
   }

   private boolean runsTopToBottom(Class<? extends Annotation> annotation) {
      return annotation.equals(Before.class) || annotation.equals(BeforeClass.class);
   }

   private List<Class<?>> getSuperClasses(Class<?> testClass) {
      ArrayList<Class<?>> results = new ArrayList();

      for(Class<?> current = testClass; current != null; current = current.getSuperclass()) {
         results.add(current);
      }

      return results;
   }

   public Class<?> getJavaClass() {
      return this.fClass;
   }

   public String getName() {
      return this.fClass == null ? "null" : this.fClass.getName();
   }

   public Constructor<?> getOnlyConstructor() {
      Constructor<?>[] constructors = this.fClass.getConstructors();
      Assert.assertEquals(1L, (long)constructors.length);
      return constructors[0];
   }

   public Annotation[] getAnnotations() {
      return this.fClass == null ? new Annotation[0] : this.fClass.getAnnotations();
   }

   public <T> List<T> getAnnotatedFieldValues(Object test, Class<? extends Annotation> annotationClass, Class<T> valueClass) {
      List<T> results = new ArrayList();

      for(FrameworkField each : this.getAnnotatedFields(annotationClass)) {
         try {
            Object fieldValue = each.get(test);
            if (valueClass.isInstance(fieldValue)) {
               results.add(valueClass.cast(fieldValue));
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException("How did getFields return a field we couldn't access?", e);
         }
      }

      return results;
   }

   public boolean isANonStaticInnerClass() {
      return this.fClass.isMemberClass() && !Modifier.isStatic(this.fClass.getModifiers());
   }
}
