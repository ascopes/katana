/**
 * Script to automatically generate the SettingsCollection class from the definition of the Settings annotation.
 * Automating this reduces the number of places we have to amend each time potential settings are added or changed.
 */


import com.squareup.javapoet.*

import javax.lang.model.element.Modifier
import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

class SettingsBoilerplateGenerator {
    private Properties properties
    private String packageName
    private SortedSet<Method> settingsMethods
    private Path generatedSourcesPath
    private ClassName settingClassName
    private ClassName rawSettingsClassName
    private ClassName settingSchemaClassName
    private Class<?> settingsAnnotationClass

    SettingsBoilerplateGenerator(Properties properties) {
        this.properties = properties

        def settingsMethods = new TreeSet<Method>({ m1, m2 -> m1.name.compareTo(m2.name) })
        def settingsAnnotationClass = Class.forName(property("settingsClasses.settingsAnnotationClass"))

        for (method in settingsAnnotationClass.declaredMethods) {
            settingsMethods.add(method)
        }

        this.packageName = property("settingsClasses.packageName")
        this.settingsMethods = Collections.unmodifiableSortedSet(settingsMethods)
        this.generatedSourcesPath = Paths.get(property("settingsClasses.generatedSourcesPath"))
        this.settingClassName = ClassName.get(this.packageName, "Setting")
        this.rawSettingsClassName = ClassName.get(this.packageName, "RawSettings")
        this.settingSchemaClassName = ClassName.get(this.packageName, "SettingSchema")
        this.settingsAnnotationClass = settingsAnnotationClass
    }

    void generate() {
        Stream.of(
                this.generateSettingsCollection(),
                this.generateSettingsMapper()
        ).forEach { this.writeOut(it) }
    }

    private TypeSpec generateSettingsCollection() {
        def typeBuilder = TypeSpec
                .classBuilder("SettingsCollection")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        def constructorMethodBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)

        this.settingsMethods.forEach {
            def name = it.name
            def type = typeNameOf it.returnType

            // Generate descriptor field
            def fieldType = ParameterizedTypeName.get(this.settingClassName, type)

            def constructorParameter = ParameterSpec
                    .builder(fieldType, name)
                    .build()

            def constructorStatement = CodeBlock
                    .of('this.$1L = $1L', name)

            def field = FieldSpec
                    .builder(fieldType, name, Modifier.PRIVATE)
                    .build()

            def getter = MethodSpec
                    .methodBuilder(getter(name))
                    .returns(field.type)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement('return this.$L', name)
                    .build()

            constructorMethodBuilder
                    .addParameter(constructorParameter)
                    .addStatement(constructorStatement)

            typeBuilder
                    .addField(field)
                    .addMethod(getter)
        }

        return typeBuilder
                .addMethod(constructorMethodBuilder.build())
                .build()
    }

    private TypeSpec generateSettingsMapper() {
        def constructor = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build()

        def flattenMethod = this.generateFlattenMethod()
        def settingMethodAccessor = this.getExpectedSettingsAccessor()

        return TypeSpec
                .classBuilder("AbstractSettingsMapper")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addMethod(constructor)
                .addMethod(this.generateMapMethod(settingMethodAccessor, flattenMethod))
                .addMethod(flattenMethod)
                .addMethod(settingMethodAccessor)
                .build()
    }

    private MethodSpec generateFlattenMethod() {
        def flattenTypeVar = TypeVariableName.get("T")
        def settingType = ParameterizedTypeName.get(settingClassName, flattenTypeVar)
        def paramType = ParameterizedTypeName.get(ClassName.get(List.class), settingType)

        return MethodSpec
                .methodBuilder("flattenSettingEntries")
                .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                .addTypeVariable(flattenTypeVar)
                .addParameter(paramType, "settings")
                .build()
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    private MethodSpec getExpectedSettingsAccessor() {
        def codeBlock = CodeBlock
                .builder()
                .beginControlFlow("try")
                .addStatement('return $T.class.getDeclaredMethod(methodName)', typeNameOf(this.settingsAnnotationClass))
                .endControlFlow()
                .beginControlFlow('catch ($T ex)', typeNameOf(Exception))
                .addStatement('throw new $T("Failed to find method " + methodName, ex)', typeNameOf(IllegalStateException))
                .endControlFlow()
                .build()

        return MethodSpec
                .methodBuilder("getExpectedSettingsAccessor")
                .addModifiers(Modifier.PRIVATE)
                .returns(Method)
                .addParameter(String.class, "methodName")
                .addCode(codeBlock)
                .build()
    }

    private MethodSpec generateMapMethod(MethodSpec settingMethodAccessor, MethodSpec flattenMethod) {
        def rawSettingsList = ParameterizedTypeName
                .get(ClassName.get(List.class), this.rawSettingsClassName)

        def methodBuilder = MethodSpec
                .methodBuilder("mapSettings")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(rawSettingsList, "settingsEntries")

        this.settingsMethods.forEach { Method it ->
            def name = it.name
            def type = it.returnType

            def schemaStatement = this.generateSettingSchemaFor(name, type, settingMethodAccessor)
            methodBuilder.addStatement(schemaStatement)
        }

        return methodBuilder
                .build()
    }

    private CodeBlock generateSettingSchemaFor(String name, Class<?> type, MethodSpec settingMethodAccessor) {
        def equalityMethod

        if (type.isArray()) {
            if (type.componentType.isPrimitive()) {
                // Primitive arrays are incompatible with Arrays.deepEquals by the looks.
                equalityMethod = CodeBlock.of('$T::equals', ClassName.get(Arrays.class))
            } else {
                // Non-primitive arrays have to be upcast.
                def arrays = ClassName.get(Arrays.class)
                def objArray = ArrayTypeName.of(Object)
                equalityMethod = CodeBlock.of('(a, b) -> $1T.deepEquals(($2T) a, ($2T) b)', arrays, objArray)
            }
        } else {
            equalityMethod = CodeBlock.of('$T::equals', ClassName.get(Objects))
        }

        return CodeBlock.of(
                'new $1T($2S, $3T.class, this.$4L($2S), $5L)',
                this.settingSchemaClassName,
                name,
                typeNameOf(type),
                settingMethodAccessor.name,
                equalityMethod
        )
    }

    private void writeOut(TypeSpec typeSpec) {
        JavaFile
                .builder(this.packageName, typeSpec)
                .skipJavaLangImports(true)
                .indent("  ")
                .build()
                .writeTo(this.generatedSourcesPath)
    }

    private String property(String name) {
        String value = this.properties.getProperty name
        return Objects.requireNonNull(value) {
            Objects.toString("Property '$name' was not specified to build script!")
        }
    }

    private static TypeName typeNameOf(Class<?> type) {
        if (type.isArray()) {
            // Arrays have to be constructed in a special way!
            return ArrayTypeName.of(type.componentType)
        } else if (type.typeParameters.length > 0) {
            // Generics also have to be constructed in a special way!
            return ParameterizedTypeName.get(type, type.typeParameters)
        } else {
            // Anything else should be easy.
            return ClassName.get(type)
        }
    }

    private static String getter(String fieldName) {
        assert fieldName.length() > 1
        def firstChar = fieldName.substring(0, 1).toUpperCase Locale.ROOT
        def rest = fieldName.substring 1
        return "get" + firstChar + rest
    }
}

//noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
new SettingsBoilerplateGenerator(properties).generate()
