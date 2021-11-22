#!/usr/bin/env groovy
//file:noinspection GrMethodMayBeStatic

/*
 * Build script which looks at the @Settings annotation in katana-annotations and will generate
 * the boilerplate SettingsCollection definition, as well as the list of SettingSchema objects
 * that define how to initialize and parse each setting.
 *
 * This should be able to automatically handle new settings with no further logic changes in
 * katana-annotation-processor, so should improve maintainability as we add more and more
 * settings going forwards.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import io.ascopes.katana.annotations.Generated
import io.ascopes.katana.annotations.Settings
import io.ascopes.katana.annotations.internal.ImmutableDefaultAdvice
import io.ascopes.katana.annotations.internal.MutableDefaultAdvice

import javax.lang.model.element.Modifier
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import java.util.stream.Stream

class CodegenSettingSchema {
  String name
  TypeName genericType
  Class<?> rawType
  CodeBlock immutableDefaultValue
  CodeBlock mutableDefaultValue
}

CodegenSettingSchema buildSchemaFor(Method method) {
  return new CodegenSettingSchema(
      name: method.name,
      genericType: TypeName.get(method.genericReturnType).box(),
      rawType: method.returnType,
      immutableDefaultValue: getDefaultValueFor(method, ImmutableDefaultAdvice),
      mutableDefaultValue: getDefaultValueFor(method, MutableDefaultAdvice),
  )
}

CodeBlock getDefaultValueFor(Method method, Class<Annotation> annotation) {
  Annotation annotationInstance = method.getAnnotation(annotation)
  String[] defaultValue
  if (annotationInstance == null) {
    assert method.defaultValue != null : "No default value for $method.name provided!"
    return stringifyDefaultValue(method.defaultValue, method.returnType)
  } else {
    defaultValue = (String[]) annotation.getMethod("value").invoke(annotationInstance)
    return parseDefaultValue(Arrays.asList(defaultValue), method.returnType)
  }
}

CodeBlock parseDefaultValue(List<String> exprs, Class<?> targetType) {
  if (targetType.isArray()) {
    String dimensionName = targetType.componentType.canonicalName
    // XXX: how do I make multiple dimensions work? Do I even care?
    return exprs.stream()
        .map { parseDefaultValue([it], targetType.componentType) }
        .collect(CodeBlock.joining(", ", "new $dimensionName[]{", "}"))
  }

  assert exprs.size() == 1: "Expected 1 expression, got ${exprs}"
  String expr = exprs[0]

  if (targetType.isEnum()) {
    Enum enumValue = Enum.valueOf((Class) targetType, expr)
    return CodeBlock.of('$N', enumValue)
  }
  if (Class.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L.class', expr)
  }
  if (byte.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Byte.parseByte(expr))
  }
  if (short.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Short.parseShort(expr))
  }
  if (int.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Integer.parseInt(expr))
  }
  if (long.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Long.parseLong(expr))
  }
  if (char.isAssignableFrom(targetType)) {
    assert expr.length() == 1: "Strings for char literals must be 1 char, but I got '$expr'"
    return CodeBlock.of('$L', expr.charAt(0))
  }
  if (float.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Float.parseFloat(expr))
  }
  if (double.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Double.parseDouble(expr))
  }
  if (boolean.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', Boolean.parseBoolean(expr))
  }

  return CodeBlock.of('$S', expr)
}

CodeBlock stringifyDefaultValue(Object value, Class<?> targetType) {
  // Important note:
  // When we have a case such as
  //    enum Foo implements Runnable {
  //        BAR {
  //            @Override
  //            public void run() { ... }
  //        };
  //    }
  // ... then the enum member Foo.BAR is actually an anonymous subclass of Foo, so
  // we cant use the literal Foo.BAR alone to determine it's value. That is why we still pass
  // the type around here.

  if (value.class.isArray()) {
    String dimensionName = value.class.componentType.canonicalName
    // XXX: how do I make multiple dimensions work? Do I even care?
    return Stream
        .of((Object[]) value)
        .map { stringifyDefaultValue(it, targetType.getComponentType()) }
        .collect(CodeBlock.joining(", ", "new $dimensionName[]{", "}"))
  }

  if (value instanceof Enum) {
    return CodeBlock.of('$T.$L', targetType, value.name())
  }

  if (value instanceof Class<?>) {
    return CodeBlock.of('$T.class', value)
  }

  if (value instanceof String) {
    return CodeBlock.of('$S', value)
  }

  return CodeBlock.of('$L', value)
}

AnnotationSpec buildGeneratedAnnotation() {
  OffsetDateTime now = OffsetDateTime.now(Clock.systemDefaultZone())
  String nowString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)

  return AnnotationSpec
      .builder(Generated)
      .addMember("name", '$S', "katana-annotation-processor build scripts")
      .addMember("date", '$S', nowString)
      .build()
}

JavaFile buildSettingsCollectionClass(
    String packageName,
    String settingPackageName,
    String settingClassName,
    List<CodegenSettingSchema> settings
) {
  ClassName settingsCollectionTypeName = ClassName.get(packageName, "SettingsCollection")
  ClassName builderTypeName = settingsCollectionTypeName.nestedClass("Builder")
  ClassName settingTypeName = ClassName.get(settingPackageName, settingClassName)

  MethodSpec builderMethod = MethodSpec
      .methodBuilder("builder")
      .returns(builderTypeName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addStatement('return new $1T()', builderTypeName)
      .build()

  MethodSpec buildMethod = MethodSpec
      .methodBuilder("build")
      .returns(settingsCollectionTypeName)
      .addModifiers(Modifier.PUBLIC)
      .addStatement('return new $T(this)', settingsCollectionTypeName)
      .build()

  MethodSpec privateConstructor = MethodSpec
      .constructorBuilder()
      .addModifiers(Modifier.PRIVATE)
      .build()

  TypeSpec.Builder type = TypeSpec
      .classBuilder(settingsCollectionTypeName.simpleName())
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addMethod(builderMethod)

  TypeSpec.Builder typeBuilder = TypeSpec
      .classBuilder(builderTypeName.simpleName())
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
      .addMethod(buildMethod)
      .addMethod(privateConstructor)

  MethodSpec.Builder typeConstructor = MethodSpec
      .constructorBuilder()
      .addModifiers(Modifier.PRIVATE)
      .addParameter(builderTypeName, "builder")

  settings.forEach {
    // The type may be primitive. If this is the case, we will need to box it first
    // (e.g. int -> java.lang.Integer; boolean -> java.lang.Boolean; etc)
    ParameterizedTypeName settingType = ParameterizedTypeName
        .get(settingTypeName, it.genericType)

    FieldSpec field = FieldSpec.builder(settingType, it.name, Modifier.PRIVATE).build()
    ParameterSpec param = ParameterSpec.builder(settingType, it.name).build()

    MethodSpec getter = MethodSpec
        .methodBuilder(attributeMethod("get", it.name))
        .returns(settingType)
        .addModifiers(Modifier.PUBLIC)
        .addStatement('return this.$N', param.name)
        .build()

    type.addField(field.toBuilder().addModifiers(Modifier.FINAL).build())
        .addMethod(getter)

    typeConstructor
        .addStatement('this.$1N = $2T.requireNonNull(builder.$1N)', it.name, Objects)

    MethodSpec builderSetter = MethodSpec
        .methodBuilder(it.name)
        .returns(builderTypeName)
        .addParameter(param)
        .addModifiers(Modifier.PUBLIC)
        .addStatement('this.$1N = $2T.requireNonNull($1N)', it.name, Objects)
        .addStatement('return this')
        .build()

    typeBuilder
        .addField(field)
        .addMethod(builderSetter)
  }

  TypeSpec settingsCollectionType = type
      .addMethod(typeConstructor.build())
      .addType(typeBuilder.build())
      .addAnnotation(buildGeneratedAnnotation())
      .build()

  return JavaFile
      .builder(packageName, settingsCollectionType)
      .build()
}

String attributeMethod(String prefix, String name) {
  return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1)
}

JavaFile buildSchemaConstants(
    String packageName,
    TypeSpec dataClass,
    String settingSchemaPackageName,
    String settingSchemaClassName,
    List<CodegenSettingSchema> schemas
) {
  ClassName builderType = ClassName.get(packageName, dataClass.name, dataClass.typeSpecs.get(0).name)
  ClassName settingsSchema = ClassName.get(settingSchemaPackageName, settingSchemaClassName)
  CodeBlock schemaDescriptors = schemas
      .stream()
      .map {
        CodeBlock.builder()
            .add('new $T<$T>(\n', settingsSchema, it.genericType)
            .indent()
            .add('$S', it.name).add(",\n")
            .add('$T.class', it.rawType).add(",\n")
            .add(it.immutableDefaultValue).add(",\n")
            .add(it.mutableDefaultValue).add(",\n")
            .add('$T::$N', builderType, it.name).add("\n")
            .unindent()
            .add(")")
            .build()
      }
      .collect(CodeBlock.joining(",\n"))

  CodeBlock streamReturn = CodeBlock
      .builder()
      .add('return $T.of(', Stream).add("\n")
      .indent()
      .add(schemaDescriptors)
      .unindent()
      .addStatement(")")
      .build()

  TypeName streamType = ParameterizedTypeName.get(
      ClassName.get(Stream),
      ParameterizedTypeName.get(settingsSchema, WildcardTypeName.subtypeOf(Object))
  )

  MethodSpec schemaSupplier = MethodSpec
      .methodBuilder("schemas")
      .returns(streamType)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addCode(streamReturn)
      .build()

  MethodSpec deadConstructor = MethodSpec
      .constructorBuilder()
      .addModifiers(Modifier.PRIVATE)
      .addStatement('throw new $1T("static-only class")', UnsupportedOperationException)
      .build()

  TypeSpec type = TypeSpec
      .classBuilder("SettingsSchemas")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addAnnotation(buildGeneratedAnnotation())
      .addMethod(deadConstructor)
      .addMethod(schemaSupplier)
      .build()

  return JavaFile
      .builder(packageName, type)
      .build()
}


List<CodegenSettingSchema> schemas = Stream
    .of(Settings.getDeclaredMethods())
    .map { buildSchemaFor(it) }
    .collect(Collectors.toList())

String getMavenProperty(String name) {
  //noinspection GrUnresolvedAccess
  String property = project.properties[name]
  String err = "Maven Property " + name + " was not set"
  return Objects.requireNonNull(property, err)
}

System.err.println("Generating setting schema definitions from @Settings annotation")

String generatedPackageName = getMavenProperty("settings.generatedPackageName")
String generatedOutputRoot = getMavenProperty("settings.generatedOutputRoot")
String settingPackageName = getMavenProperty("settings.settingPackageName")
String settingClassName = getMavenProperty("settings.settingClassName")
String settingSchemaPackageName = getMavenProperty("settings.settingSchemaPackageName")
String settingSchemaClassName = getMavenProperty("settings.settingSchemaClassName")

JavaFile dataClass = buildSettingsCollectionClass(
    generatedPackageName,
    settingPackageName,
    settingClassName,
    schemas
)

JavaFile schemaDefinition = buildSchemaConstants(
    generatedPackageName,
    dataClass.typeSpec,
    settingSchemaPackageName,
    settingSchemaClassName,
    schemas
)

// Can't use Path.of in Java 8 builds
Path outputPath = Paths.get(generatedOutputRoot).toAbsolutePath()
System.err.printf("Writing out generated code to %s%n", outputPath)

dataClass.writeTo(outputPath)
schemaDefinition.writeTo(outputPath)

System.err.println("Done")