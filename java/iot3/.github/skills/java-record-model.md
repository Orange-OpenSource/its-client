# Skill: Java Record Model & Builder Pattern

## Java Version

Java 16+ is required. Use **records** for all model/POJO classes — never plain classes with getters/setters for message data structures.

## Javadoc on Records

Document every record component with a `@param` tag at class level:

```java
/**
 * Short description.
 *
 * @param foo Description with unit or allowed values if relevant.
 * @param bar Optional. Description.
 */
public record MyRecord(String foo, Integer bar) {}
```

## Builder Pattern

Builders are always a `public static final class Builder` nested inside the record. Use a private `requireNonNull` helper — **do not** use `java.util.Objects.requireNonNull` which throws `NullPointerException` with a less descriptive message for builder misuse:

```java
public record MyRecord(String foo, Integer bar) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String foo;
        private Integer bar;

        private Builder() {}

        public Builder foo(String foo) {
            this.foo = foo;
            return this;
        }

        public Builder bar(Integer bar) {
            this.bar = bar;
            return this;
        }

        public MyRecord build() {
            return new MyRecord(
                requireNonNull(foo, "foo"),
                requireNonNull(bar, "bar")
            );
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
```

All fields are mandatory by default. Calling `build()` with any `null` required field throws `IllegalStateException("Missing field: …")`. Optional fields are passed directly without `requireNonNull`.

The `type` and `version` fields (when present in envelope records) are **hardcoded in the builder's private constructor** — do not expose setters for them.

