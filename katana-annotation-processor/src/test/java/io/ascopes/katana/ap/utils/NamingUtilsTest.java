package io.ascopes.katana.ap.utils;

import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;

class NamingUtilsTest {

  @ParameterizedTest
  @MethodSource("hardKeywords")
  void can_transmogrify_hard_keywords(String id) {
    String result = NamingUtils.transmogrifyIdentifier(id);

    BDDAssertions
        .then(result)
        .matches(SourceVersion::isIdentifier);
  }

  @ParameterizedTest
  @MethodSource({"validNames", "softKeywords"})
  void valid_names_pass_identifier_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateIdentifier(id))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EmptySource
  void empty_names_fail_identifier_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateIdentifier(id))
        .hasMessage("name '" + id + "' cannot be empty");
  }

  @ParameterizedTest
  @MethodSource("hardKeywords")
  void hard_keywords_fail_identifier_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateIdentifier(id))
        .hasMessage("name '" + id + "' is a reserved keyword in Java");
  }

  @ParameterizedTest
  @MethodSource("invalidNames")
  void invalid_names_fail_identifier_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateIdentifier(id))
        .hasMessage("name '" + id + "' is not a valid Java identifier");
  }

  @ParameterizedTest
  @MethodSource({"validNames", "softKeywords"})
  void valid_names_pass_class_name_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateClassName(id))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EmptySource
  void empty_names_fail_class_name_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateClassName(id))
        .hasMessage("invalid class name '" + id + "': name '" + id + "' cannot be empty");
  }

  @ParameterizedTest
  @MethodSource("hardKeywords")
  void hard_keywords_fail_class_name_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateClassName(id))
        .hasMessage(
            "invalid class name '" + id + "': name '" + id + "' is a reserved keyword in Java"
        );
  }

  @ParameterizedTest
  @MethodSource("invalidNames")
  void invalid_names_fail_class_name_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validateClassName(id))
        .hasMessage(
            "invalid class name '" + id + "': name '" + id + "' is not a valid Java identifier"
        );
  }

  @ParameterizedTest
  @MethodSource({"validNames", "softKeywords", "validPackageNames"})
  @EmptySource
  void valid_names_pass_package_name_validation(String id) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validatePackageName(id))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @MethodSource("invalidPackageNamesWithHardKeywords")
  void hard_keywords_fail_package_name_validation(String packageName, String badId) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validatePackageName(packageName))
        .hasMessage(
            "invalid package name '" + packageName + "': name '" + badId
                + "' is a reserved keyword in Java"
        );
  }

  @ParameterizedTest
  @MethodSource("invalidPackageNamesWithInvalidNames")
  void invalid_names_fail_package_name_validation(String packageName, String badId) {
    BDDAssertions
        .thenCode(() -> NamingUtils.validatePackageName(packageName))
        .hasMessage(
            "invalid package name '" + packageName + "': name '" + badId
                + "' is not a valid Java identifier"
        );
  }

  static Stream<String> validNames() {
    return Stream.of(
        "foo", "bar", "baz", "bork", "HelloWorld", "name_with_underscores",
        "STOP_SHOUTING_AT_ME", "fooBarBaz", "SeeingDollar$igns", "_sunder_",
        "__dunder__", "___tunder___", "thisCont41n5Numb3r5"
    );
  }

  static Stream<String> softKeywords() {
    return Stream.of("permits", "record", "sealed", "var", "yield");
  }

  static Stream<String> hardKeywords() {
    return Stream.of(
        "strictfp", "assert", "enum", "_",
        "public", "protected", "private", "abstract", "static", "final",
        "transient", "volatile", "synchronized", "native",
        "class", "interface", "extends", "package", "throws",
        "implements",
        "boolean", "byte", "char", "short", "int", "long", "float", "double",
        "void",
        "if", "else", "try", "catch", "finally", "do", "while", "for", "continue",
        "switch", "case", "default", "break", "throw", "return",
        "this", "new", "super", "import", "instanceof",
        "goto", "const",
        "null", "true", "false"
    );
  }

  static Stream<String> invalidNames() {
    return Stream.of(
        " ", "\r", "\n", "\t", "\f", "123StartsWithNumbers",
        ":-)", "?", "++", "=", "@", "\"", "!", "hello world",
        "spam*", "*eggs", "*who*", "*", ";", "what;", ";where",
        "why;though"
    );
  }

  static Stream<String> validPackageNames() {
    return Stream.of(
        "java.util",
        "java.lang",
        "mockito",
        "a.b.c",
        "org.springframework.boot",
        "packageName.usingCamel.caseStrings",
        "packagename.using.numbers123",
        "packagename.using.underscores_between_4_words",
        "io.ascopes.katana",
        "io.ascopes.katana.ap",
        "io.ascopes.katana.utils",
        "sun.misc"
    );
  }

  static Stream<Arguments> invalidPackageNamesWithHardKeywords() {
    return invalidPackageNamesFrom(hardKeywords());
  }

  static Stream<Arguments> invalidPackageNamesWithInvalidNames() {
    return invalidPackageNamesFrom(invalidNames());
  }

  static Stream<Arguments> invalidPackageNamesFrom(Stream<String> badNames) {
    Stream.Builder<Arguments> bldr = Stream.builder();
    badNames.forEach(kw -> bldr
        .add(Arguments.of(kw, kw))
        .add(Arguments.of("foo.bar.baz." + kw, kw))
        .add(Arguments.of(kw + ".foo.bar.baz", kw))
        .add(Arguments.of("foo.bar." + kw + ".baz.bork", kw)));
    return bldr.build();
  }

}
