/**
 * Script to automatically generate the SettingsCollection class from the definition of the Settings annotation.
 * Automating this reduces the number of places we have to amend each time potential settings are added or changed.
 */
import com.squareup.javapoet.*
import io.ascopes.katana.annotations.Settings

import javax.lang.model.element.Modifier
import java.nio.file.Paths

static def typeNameOf(def type) {
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

static def getter(def fieldName) {
    assert fieldName.length() > 1
    def firstChar = fieldName.substring(0, 1).toUpperCase Locale.ROOT
    def rest = fieldName.substring 1
    return "get" + firstChar + rest
}

static def settingsMethods() {
    def settingsMethods = []
    settingsMethods.addAll Arrays.asList(*Settings.declaredMethods)
    settingsMethods.sort { m1, m2 -> m1.name.compareTo m2.name }
    return settingsMethods
}

static def generateCollectionClass(def settingClass) {
    def typeBuilder = TypeSpec
            .classBuilder("SettingsCollection")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

    def constructorMethodBuilder = MethodSpec
            .constructorBuilder()
            .addModifiers(Modifier.PUBLIC)

    settingsMethods().forEach {
        def name = it.name
        def type = typeNameOf it.returnType

        // Generate descriptor field
        def fieldType = ParameterizedTypeName.get(settingClass, type)

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

static def generateFlattenMethod(def settingClass) {
    def flattenTypeVar = TypeVariableName.get("T")
    def settingType = ParameterizedTypeName.get(settingClass, flattenTypeVar)
    def paramType = ParameterizedTypeName.get(ClassName.get(List.class), settingType)

    return MethodSpec
            .methodBuilder("flattenSettingEntries")
            .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
            .addTypeVariable(flattenTypeVar)
            .addParameter(paramType, "settings")
            .build()
}

static def generateMapMethod(def settingClass, def settingsEntryClass, def settingLocationClass) {
    def entryListClass = ParameterizedTypeName
            .get(ClassName.get(List.class), settingsEntryClass)

    // TODO: complete

    return MethodSpec
            .methodBuilder("mapSettings")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(entryListClass, "settingsEntries")
            .build()
}

static def generateMapperClass(def settingClass, def settingsEntryClass, def settingLocationClass) {
    def constructor = MethodSpec
            .constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .build()

    settingsMethods().forEach {
        def name = it.name
        def type = typeNameOf it.returnType
    }

    return TypeSpec
            .classBuilder("AbstractSettingsMapper")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addMethod(constructor)
            .addMethod(generateFlattenMethod(settingClass))
            .addMethod(generateMapMethod(settingClass, settingsEntryClass, settingLocationClass))
            .build()
}

def generatedSourcesDir = properties["generatedSources.outputDir"]
def packageName = properties["generatedSources.package"]

// We do not refer to the actual class here, as it is not yet compiled when the script runs.
def settingClass = ClassName.get(packageName.toString(), "Setting")
def settingsEntryClass = ClassName.get(packageName.toString(), "SettingsEntry")
def settingLocationClass = ClassName.get(packageName.toString(), "SettingLocation")

// Generate the code.
def types = [
        generateCollectionClass(settingClass),
        generateMapperClass(settingClass, settingsEntryClass, settingLocationClass)
]

def fullPath = Paths
        .get(generatedSourcesDir, "target", "generated-sources", "katana")
        .toAbsolutePath()

// Write out the code.
types.forEach {
    JavaFile
            .builder(packageName, it)
            .indent("  ")
            .build()
            .writeTo fullPath
}
