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

package io.ascopes.katana.ap.methods;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.ascopes.katana.ap.attributes.AttributeDescriptor;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.NamingUtils;
import javax.lang.model.element.Modifier;


/**
 * Factory for generating setters.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SetterSourceFactory {

  private final Logger logger;

  /**
   * Initialize this factory.
   */
  public SetterSourceFactory() {
    this.logger = LoggerFactory.loggerFor(this.getClass());
  }

  /**
   * Create a setter for the given attribute.
   *
   * @param attributeDescriptor the attribute to generate the setter for.
   * @param setterPrefix        the setter prefix to use.
   * @return the generated method spec.
   */
  public MethodSpec create(AttributeDescriptor attributeDescriptor, String setterPrefix) {
    String setterName = NamingUtils.addPrefixCamelCase(setterPrefix, attributeDescriptor.getName());

    ParameterSpec parameter = ParameterSpec
        .builder(attributeDescriptor.getType(), attributeDescriptor.getIdentifier())
        .addModifiers(Modifier.FINAL)
        .build();

    MethodSpec.Builder builder = MethodSpec
        .methodBuilder(setterName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(parameter)
        .addStatement("this.$1L = $1L", attributeDescriptor.getIdentifier());

    attributeDescriptor
        .getDeprecatedAnnotation()
        .map(AnnotationSpec::get)
        .ifPresent(builder::addAnnotation);

    MethodSpec method = builder.build();
    this.logger.trace("Generated setter\n{}", method);
    return method;
  }
}
