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

package io.ascopes.katana.ap;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import io.ascopes.katana.ap.logging.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;

class BareModelIntegrationTest {

  KatanaProcessor processor;

  @BeforeEach
  void setUp() {
    this.processor = new KatanaProcessor();
    LoggerFactory.globalLevel("TRACE");
  }

  @Test
  void test_bare_model() {
    Compilation result = Compiler
        .javac()
        .withProcessors(this.processor)
        .compile(
            forSourceLines(
                "test.package-info",
                "@ImmutableModel",
                "package test;",
                "import io.ascopes.katana.annotations.ImmutableModel;",
                "import io.ascopes.katana.annotations.Settings;"
            ),
            forSourceLines(
                "test.BareModel",
                "package test;",
                "import io.ascopes.katana.annotations.MutableModel;",
                "import io.ascopes.katana.annotations.Builder;",
                "import io.ascopes.katana.annotations.Settings;",
                "import io.ascopes.katana.annotations.ToString;",
                "import io.ascopes.katana.annotations.ImmutableModel;",
                "import java.util.concurrent.atomic.AtomicBoolean;",
                "",
                "@MutableModel",
                "@ImmutableModel",
                "@Settings(builder = Builder.TYPESAFE)",
                "public interface BareModel {",
                "  String getFoo();",
                "  int getBar();",
                "  AtomicBoolean isBaz();",
                "  boolean isBork();",
                "  Boolean isQux();",
                "  @Deprecated",
                "  boolean getQuxx();",
                "  ",
                "  @ToString.CustomToString",
                "  static String asString(BareModel model) {",
                "    return \"hello world!\";",
                "  }",
                "  ",
                "}"
            )
        );

    assertThat(result)
        .succeededWithoutWarnings();
  }
}
