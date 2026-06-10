# Skill: Java Enum Pattern

## Enum Structure

Enums expose a **public final field `value`** (not a getter) and a **static `fromValue()` factory** for reverse lookup. The value type matches the JSON encoding: `int` for numeric fields, `String` for string fields.

### Numeric enum (int value)

```java
/**
 * Description of what the enum represents.
 */
public enum McmType {
    INTENT(0),
    REQUEST(1),
    RESPONSE(2);

    public final int value;

    McmType(int value) {
        this.value = value;
    }

    public static McmType fromValue(int value) {
        for (McmType mcmType : values()) {
            if (mcmType.value == value) {
                return mcmType;
            }
        }
        throw new IllegalArgumentException("Unknown MCM type: " + value);
    }
}
```

### String enum (String value)

```java
/**
 * Description of what the enum represents.
 */
public enum ManoeuvreStrategy {
    DRIVE_STRAIGHT("drive_straight"),
    TURN_LEFT("turn_left"),
    STOP("stop");

    public final String value;

    ManoeuvreStrategy(String value) {
        this.value = value;
    }

    public static ManoeuvreStrategy fromValue(String value) {
        for (ManoeuvreStrategy strategy : values()) {
            if (strategy.value.equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown manoeuvre strategy: " + value);
    }
}
```

## Key Rules

- Field is always `public final` named `value` — never use a getter method.
- `fromValue()` iterates over `values()` and throws `IllegalArgumentException` with a descriptive message on unknown input.
- Loop variable name follows the naming convention: camelCase form of the enum type name (e.g. `McmType` → `mcmType`, `Concept` → `concept`).

## Usage in Record Builders

When a record field is backed by an enum, the builder provides **two overloads**: one accepting the raw type (`int` or `String`) and one accepting the typed enum. The record itself stores the raw value.

```java
public record MyContainer(int mcmType, int concept) {

    public static final class Builder {
        private Integer mcmType;
        private Integer concept;

        // Raw int setter
        public Builder mcmType(int mcmType) {
            this.mcmType = mcmType;
            return this;
        }

        // Typed enum setter — accesses .value directly
        /**
         * Sets the MCM type using the typed enum constant.
         *
         * @param mcmType {@link McmType} value
         * @return this builder
         */
        public Builder mcmType(McmType mcmType) {
            this.mcmType = mcmType.value;
            return this;
        }

        // same dual pattern for concept...
    }
}
```

## Usage in Jackson Streaming Readers

In Reader classes, use `fromValue()` when decoding an enum from JSON:

```java
case "mcm_type" -> mcmType = McmType.fromValue(parser.getIntValue());
case "manoeuvre_overall_strategy" -> strategy = ManoeuvreStrategy.fromValue(parser.getValueAsString());
```

## Usage in Jackson Streaming Writers

Writers do not need the enum types — they write the raw values directly from the record accessors:

```java
generator.writeNumberField("mcm_type", container.mcmType());
generator.writeStringField("manoeuvre_overall_strategy", submanoeuvre.strategy());
```


