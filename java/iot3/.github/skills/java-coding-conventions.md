# Skill: Java Coding Conventions (Summary)

This is the umbrella skill for all Java IoT3 coding conventions. When working on Java files under `java/iot3/`, apply the rules from the following individual skills:

| Skill | Applies to |
|-------|-----------|
| [`java-license-header`](java-license-header.md) | Every new `.java` file |
| [`java-record-model`](java-record-model.md) | Model/POJO records, builders |
| [`java-enum-pattern`](java-enum-pattern.md) | Enums with `value` / `fromValue()`, dual builder setters |
| [`java-jackson-streaming`](java-jackson-streaming.md) | Codecs, readers, writers |
| [`java-naming-conventions`](java-naming-conventions.md) | All Java code |
| [`java-testing`](java-testing.md) | Test files |

## Quick Checklist

- [ ] License header present (with `@generated` if AI-generated)
- [ ] Java 16+ records for model classes
- [ ] Builder with private `requireNonNull` (throws `IllegalStateException`)
- [ ] Enums use `public final value` + `static fromValue()`
- [ ] Builders offer dual setters (raw + enum) for enum-backed fields
- [ ] Jackson streaming only — no `ObjectMapper`, `@JsonProperty`, `JsonNode`
- [ ] Full descriptive variable names — no single-letter abbreviations
- [ ] Tests use JUnit 5 + Mockito only

