package io.ascopes.katana.spi.annotationprocessor

import io.ascopes.katana.spi.annotations.ServiceProvider
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation


/**
 * Annotation processor for generating `META-INF/services` files from annotation classes.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
class KatanaSpiAnnotationProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes() = setOf(ServiceProvider::class.java.canonicalName)

  override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    annotations
        .flatMap { annotationType ->
          roundEnv
              .getElementsAnnotatedWith(annotationType)
              .filterIsInstance(TypeElement::class.java)
              .mapNotNull { getServiceTypePair(annotationType, it) }
        }
        .groupBy { it.provider }
        .entries
        .forEach { (provider, impls) -> writeImplementations(provider, impls) }

    return false
  }

  private fun getServiceTypePair(
      annotationType: TypeElement,
      annotatedType: TypeElement
  ): ServiceLoaderEntry? {
    val messager = processingEnv.messager
    val types = processingEnv.typeUtils

    val annotationTypeMirror = annotationType.asType()
    val annotatedTypeMirror = annotatedType.asType()
    val annotationMirror = annotatedType.annotationMirrors
        .first { types.isSameType(it.annotationType, annotationTypeMirror) }

    if (annotationType.kind == ElementKind.CLASS || annotatedType.kind == ElementKind.INTERFACE) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          "${annotatedType.qualifiedName} has kind ${annotatedType.kind}, which is not a " +
              "regular class or interface",
          annotatedType,
          annotationMirror
      )
      return null
    }

    val providerTypeMirror = annotationMirror.elementValues
        .values
        .first()
        .value as TypeMirror
    val providerType = types.asElement(providerTypeMirror) as TypeElement

    if (!types.isSubtype(annotatedTypeMirror, providerTypeMirror)) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          "${annotatedType.qualifiedName} is not a subtype of ${providerType.qualifiedName}",
          annotatedType,
          annotationMirror
      )
      return null
    }

    return ServiceLoaderEntry(
        providerType.qualifiedName.toString(),
        annotatedType.qualifiedName.toString()
    )
  }

  private fun writeImplementations(providerName: String, impls: List<ServiceLoaderEntry>) {
    processingEnv
        .messager
        .printMessage(
            Diagnostic.Kind.NOTE,
            "Writing ${impls.size} class(es) to service file for $providerName"
        )

    processingEnv
        .filer
        .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/$providerName")
        // Delete first, if it exists.
        .apply { delete() }
        .openWriter()
        .buffered()
        .use { writer ->
          impls
              // Remove duplicates and order lexicographically.
              .toSortedSet()
              .forEach {
                writer.write(it.implementation)
                writer.write("\n")
              }
        }
  }

  private data class ServiceLoaderEntry(
      val provider: String,
      val implementation: String
  ) : Comparable<ServiceLoaderEntry> {
    override fun compareTo(other: ServiceLoaderEntry) = comparator.compare(this, other)

    private companion object {
      @Suppress("RedundantLambdaArrow")
      val comparator = compareBy { it: ServiceLoaderEntry -> it.provider }
          .thenComparing { it: ServiceLoaderEntry -> it.implementation }!!
    }
  }
}