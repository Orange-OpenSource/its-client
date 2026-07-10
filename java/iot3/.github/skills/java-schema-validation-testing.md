# Skill: JSON Schema Validation Testing

## Purpose

Use this skill when writing tests that verify a serialized JSON message conforms to its ETSI JSON Schema file.

## Location Convention

Schema validation test classes live in a `schema` sub-package under the version package:

```
messages/{type}/v{version}/schema/{Type}Schema{Version}Test.java
```

Examples:
- `messages/cam/v240/schema/CamSchema240Test.java`
- `messages/denm/v230/schema/DenmSchema230Test.java`
- `messages/cpm/v211/schema/CpmSchema211Test.java`

## Schema Loading

Load the schema **once** in a `static {}` block using `SchemaTestUtils.loadSchema()`.
Pass a path relative to the repository `schema/` directory:

```java
private static final JsonSchema SCHEMA;

static {
    try {
        SCHEMA = SchemaTestUtils.loadSchema("cam/cam_schema_2-4-0.json");
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

## Encoding and Asserting

Encode the message with the type-specific codec (not `ObjectMapper`) and validate the raw bytes:

```java
@Test
void write_minimalValidEnvelope_conformsToSchema() throws Exception {
    CamEnvelope240 envelope = minimalEnvelope();
    CamCodec codec = new CamCodec(new JsonFactory());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    codec.write(CamVersion.V2_4_0, envelope, out);

    SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
}
```

## Test Method Naming

Follow the pattern `write_{scenario}_conformsToSchema`:

| Scenario | Method name |
|---|---|
| Only mandatory fields | `write_minimalValidEnvelope_conformsToSchema` |
| All optional fields populated | `write_fullyPopulatedEnvelope_conformsToSchema` |
| Specific optional container added | `write_envelopeWith{Container}_conformsToSchema` |

## Test Helper Methods

Extract fixture construction into private static helpers to keep tests readable:

```java
private static CamEnvelope240 minimalEnvelope() { ... }
private static CamEnvelope240 fullyPopulatedEnvelope() { ... }
private static BasicContainer validBasicContainer() { ... }
private static BasicVehicleContainerHighFrequency validHighFrequencyContainer() { ... }
```

- `minimalEnvelope()` — only mandatory fields; always write this test first.
- `fullyPopulatedEnvelope()` — every optional field set to a valid value.
- `valid{Container}()` — shared sub-structures reused across multiple tests.

## SchemaTestUtils

`SchemaTestUtils` is the single shared utility class for all schema validation tests:

```java
// Load a schema from the repository schema/ directory
JsonSchema schema = SchemaTestUtils.loadSchema("cam/cam_schema_2-4-0.json");

// Assert a byte array conforms to the schema (throws AssertionError with violation details on failure)
SchemaTestUtils.assertConformsToSchema(schema, out.toByteArray());
```

- The factory uses **JSON Schema 2020-12** (`SpecVersion.VersionFlag.V202012`).
- A schema mapper automatically redirects any remote `$ref` ending in `dsrc_schema_2-0-0.json` to the local copy under `schema/dsrc/`, so no network access is required.
- Do **not** create a second `JsonSchemaFactory` in individual tests — always go through `SchemaTestUtils`.

## Checklist

- [ ] Class in the `schema` sub-package of the version package
- [ ] Schema loaded once in `static {}` via `SchemaTestUtils.loadSchema()`
- [ ] Encoding done with the type's codec + `JsonFactory` (not `ObjectMapper`)
- [ ] At least a `write_minimalValidEnvelope_conformsToSchema` test
- [ ] A `write_fullyPopulatedEnvelope_conformsToSchema` test when optional containers exist
- [ ] Fixture helpers are `private static` methods, not inline in `@Test` methods
- [ ] Validation via `SchemaTestUtils.assertConformsToSchema(SCHEMA, bytes)`
