# katana

A project that aims to provide an annotation processor for Java 8 and newer which can generate
boilerplate data classes.

This takes a lot of inspiration from [Project Lombok](https://projectlombok.org/)
and [Immutables](https://immutables.github.io/), while addressing several fundemental downsides that
I have found from using both of these tools.

The library itself is not designed to act as a replacement for
[JEP 395: Records](https://openjdk.java.net/jeps/395), but to instead compliment it and to fill in
for it on older versions of the Java Language Specification.

**This project is currently under active development and is not yet in a usable state. Feedback is
always welcome!**

An example of what to expect from this library:

```java
@ImmutableModel
@Settings(
        builder = true,
        equalityMode = Equality.EXCLUDE_ALL    // default is to consider all fields for equality.
)
public interface JsonWebToken {
    @ToString.Exclude  
    String getIssuer();

    String getSubject();

    String getAudience();
    
    ZonedDateTime getExpirationTime();
    
    @ToString.Exclude
    ZonedDateTime getNotBefore();
    
    @Equality.Include
    String getJwtId();
}
```

```java
ZonedDateTime now = ZonedDateTime.now(Clock.systemUtc());

JsonWebToken token = ImmutableJsonWebToken
        .builder()
        .issuer("Ashley")
        .subject("username")
        .audience("public-api")
        .expirationTime(now.plusMinutes(30))
        .notBefore(now)
        .jwtId(UUID.randomUuid().toString())
        .build();
```
