# katana-compiler-testing-core

Core components for compiler-testing implementations to reuse.

Unless you are extending this API, you probably do not need to include this
dependency directly.

## JPMS Modules

If you need to include this in a JPMS modular application, add the following to your
`module-info.java`:

```java
module my.modulename.here {
  requires transitive katana.compilertesting.core;
}
```