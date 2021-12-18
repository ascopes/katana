/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.ap.types;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.builders.DelegatingBuilderFactory;
import io.ascopes.katana.ap.fields.FieldSourceFactory;
import io.ascopes.katana.ap.files.CompilationUnitDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.methods.ConstructorSourceFactory;
import io.ascopes.katana.ap.methods.EqualsHashCodeSourceFactory;
import io.ascopes.katana.ap.methods.GetterSourceFactory;
import io.ascopes.katana.ap.methods.SetterSourceFactory;
import io.ascopes.katana.ap.methods.ToStringSourceFactory;
import io.ascopes.katana.ap.utils.CodeGenUtils;
import javax.lang.model.element.Modifier;

/**
 * Factory for generating Java classes from descriptor definitions.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class DataClassSourceFactory {

  private final Logger logger;
  private final FieldSourceFactory fieldSourceFactory;
  private final GetterSourceFactory getterSourceFactory;
  private final SetterSourceFactory setterSourceFactory;
  private final ConstructorSourceFactory constructorSourceFactory;
  private final DelegatingBuilderFactory delegatingBuilderFactory;
  private final EqualsHashCodeSourceFactory equalsHashCodeSourceFactory;
  private final ToStringSourceFactory toStringSourceFactory;

  /**
   * Initialize this factory.
   */
  public DataClassSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.fieldSourceFactory = new FieldSourceFactory();
    this.getterSourceFactory = new GetterSourceFactory();
    this.setterSourceFactory = new SetterSourceFactory();
    this.constructorSourceFactory = new ConstructorSourceFactory();
    this.delegatingBuilderFactory = new DelegatingBuilderFactory();
    this.equalsHashCodeSourceFactory = new EqualsHashCodeSourceFactory();
    this.toStringSourceFactory = new ToStringSourceFactory();
  }

  /**
   * Create a Java compilation unit for the given model.
   *
   * @param modelDescriptor the model to use.
   * @return the generated compilation unit.
   */
  public CompilationUnitDescriptor create(ModelDescriptor modelDescriptor) {
    this.logger.debug("Building Java file for {}", modelDescriptor);
    TypeSpec typeSpec = this.buildModelTypeSpecFrom(modelDescriptor);
    JavaFile javaFile = this.wrapTypeSpecInPackage(typeSpec, modelDescriptor);
    this.logger.trace("Generated source file {}\n{}", modelDescriptor.getQualifiedName(), javaFile);

    return new CompilationUnitDescriptor(
        modelDescriptor.getQualifiedName().toString(),
        javaFile
    );
  }

  private JavaFile wrapTypeSpecInPackage(TypeSpec typeSpec, ModelDescriptor modelDescriptor) {
    return JavaFile
        .builder(modelDescriptor.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .indent(modelDescriptor.getIndent())
        .build();
  }

  private TypeSpec buildModelTypeSpecFrom(ModelDescriptor modelDescriptor) {
    TypeSpec.Builder builder = TypeSpec
        .classBuilder(modelDescriptor.getClassName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(modelDescriptor.getSuperInterface().asType())
        .addAnnotation(CodeGenUtils.generated(modelDescriptor.getSuperInterface()));

    this.applyDeprecation(builder, modelDescriptor);
    this.applyAttributes(builder, modelDescriptor);
    this.applyConstructors(builder, modelDescriptor);
    this.applyBuilders(builder, modelDescriptor);
    this.applyEqualsHashCode(builder, modelDescriptor);
    this.applyToString(builder, modelDescriptor);

    return builder.build();
  }

  private void applyDeprecation(TypeSpec.Builder typeSpecBuilder, ModelDescriptor modelDescriptor) {
    modelDescriptor.getDeprecatedAnnotation()
        .map(CodeGenUtils::copyDeprecatedFrom)
        .ifPresent(typeSpecBuilder::addAnnotation);
  }

  private void applyAttributes(TypeSpec.Builder typeSpecBuilder, ModelDescriptor modelDescriptor) {
    for (AttributeDescriptor attributeDescriptor : modelDescriptor.getAttributes()) {
      typeSpecBuilder
          .addField(this.fieldSourceFactory.create(attributeDescriptor))
          .addMethod(this.getterSourceFactory.create(attributeDescriptor));

      if (!attributeDescriptor.isFinalField()) {
        MethodSpec setter = this.setterSourceFactory
            .create(attributeDescriptor, modelDescriptor.getSetterPrefix());

        typeSpecBuilder
            .addMethod(setter);
      }
    }
  }

  private void applyConstructors(
      TypeSpec.Builder typeSpecBuilder,
      ModelDescriptor modelDescriptor
  ) {
    this.constructorSourceFactory
        .create(modelDescriptor)
        .forEach(typeSpecBuilder::addMethod);
  }

  private void applyBuilders(
      TypeSpec.Builder typeSpecBuilder,
      ModelDescriptor modelDescriptor
  ) {
    modelDescriptor.getBuilderStrategy()
        .map(strategy -> this.delegatingBuilderFactory.create(modelDescriptor, strategy))
        .ifPresent(members -> members.applyTo(typeSpecBuilder));
  }

  private void applyEqualsHashCode(
      TypeSpec.Builder typeSpecBuilder,
      ModelDescriptor modelDescriptor
  ) {
    this.equalsHashCodeSourceFactory
        .create(modelDescriptor)
        .ifPresent(members -> members.applyTo(typeSpecBuilder));
  }

  private void applyToString(TypeSpec.Builder typeSpecBuilder, ModelDescriptor modelDescriptor) {
    this.toStringSourceFactory
        .create(modelDescriptor)
        .ifPresent(typeSpecBuilder::addMethod);
  }
}
