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
import com.squareup.javapoet.*
import io.ascopes.katana.annotations.Generated
import io.ascopes.katana.annotations.Settings
import io.ascopes.katana.annotations.internal.DefaultSetting
import io.ascopes.katana.annotations.internal.ImmutableDefaultSetting
import io.ascopes.katana.annotations.internal.MutableDefaultSetting

import javax.lang.model.element.Modifier
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.nio.file.Path
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import java.util.stream.Stream

class CodegenSettingSchema {
  String name
  Class<?> type
  CodeBlock immutableDefaultValue
  CodeBlock mutableDefaultValue
  CodeBlock inheritedValue
  CodeBlock equalityFunction
}

CodegenSettingSchema buildSchemaFor(Method method) {
  List<String> rawImmutableDefaultValue = getDefaultValueFor(method, ImmutableDefaultSetting)
  List<String> rawMutableDefaultValue = getDefaultValueFor(method, MutableDefaultSetting)

  return new CodegenSettingSchema(
      name: method.name,
      type: method.returnType,
      immutableDefaultValue: parseDefaultValue(rawImmutableDefaultValue, method.returnType),
      mutableDefaultValue: parseDefaultValue(rawMutableDefaultValue, method.returnType),
      inheritedValue: parseInheritedValue(method.defaultValue),
      equalityFunction: buildEqualityMethod(method.returnType),
  )
}

List<String> getDefaultValueFor(Method method, Class<Annotation> annotation) {
  Annotation annotationInstance = method.getAnnotation(annotation)
  String[] defaultValue
  if (annotationInstance == null) {
    DefaultSetting defaultInstance = method.getAnnotation(DefaultSetting)
    assert defaultInstance: "Missing @$DefaultSetting.simpleName on Setting $method.name"
    defaultValue = (String[]) DefaultSetting.getMethod("value").invoke(defaultInstance)
  } else {
    defaultValue = (String[]) annotation.getMethod("value").invoke(annotationInstance)
  }

  return [*defaultValue]
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
    def enumValue = Enum.valueOf((Class) targetType, expr)
    return CodeBlock.of('$T.$L', targetType, enumValue.name())
  }
  if (Class.isAssignableFrom(targetType)) {
    return CodeBlock.of('$L', expr)
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

CodeBlock parseInheritedValue(Object value) {
  if (value.class.isArray()) {
    String dimensionName = value.class.componentType.canonicalName
    // XXX: how do I make multiple dimensions work? Do I even care?
    return Stream
        .of((Object[]) value)
        .map { parseInheritedValue(it) }
        .collect(CodeBlock.joining(", ", "new $dimensionName[]{", "}"))
  }

  if (value instanceof Enum) {
    return CodeBlock.of('$T.$L', value.class, value.name())
  }

  if (value instanceof Class<?>) {
    return CodeBlock.of('$T', value)
  }

  if (value instanceof String) {
    return CodeBlock.of('$S', value)
  }

  return CodeBlock.of('$L', value)
}

CodeBlock buildEqualityMethod(Class<?> targetType) {
  if (targetType.isArray()) {
    return CodeBlock.of('(a, b) -> $1T.deepEquals(($2T[]) a, ($2T[]) b)', Arrays.class, Object.class)
  }

  return CodeBlock.of('$T::equals', Objects)
}

AnnotationSpec buildGeneratedAnnotation() {
  def now = OffsetDateTime.now(Clock.systemDefaultZone())
  def nowString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)

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
    TypeName settingInnerType = it.type.isArray()
        ? ArrayTypeName.of(ClassName.get(it.type.componentType))
        : ClassName.get(it.type)

    ParameterizedTypeName settingType = ParameterizedTypeName
        .get(settingTypeName, settingInnerType)

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
            .add('new $T<>(\n', settingsSchema)
            .indent()
            .add('$S', it.name).add(",\n")
            .add('$T.class', it.type).add(",\n")
            .add(it.inheritedValue).add(",\n")
            .add(it.immutableDefaultValue).add(",\n")
            .add(it.mutableDefaultValue).add(",\n")
            .add(it.equalityFunction).add(",\n")
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


def schemas = Stream
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

def dataClass = buildSettingsCollectionClass(
    generatedPackageName,
    settingPackageName,
    settingClassName,
    schemas
)

def schemaDefinition = buildSchemaConstants(
    generatedPackageName,
    dataClass.typeSpec,
    settingSchemaPackageName,
    settingSchemaClassName,
    schemas
)

def outputPath = Path.of(generatedOutputRoot).toAbsolutePath()
System.err.printf("Writing out generated code to %s%n", outputPath)

dataClass.writeTo(outputPath)
schemaDefinition.writeTo(outputPath)

System.err.println("Done")