# Skill: Jackson Streaming API

Use **Jackson streaming API only** (`JsonParser` / `JsonGenerator`) in all codec, reader, and writer classes.

## Forbidden

- `ObjectMapper`
- `@JsonProperty` or any Jackson annotation
- Tree model (`JsonNode`, `ObjectNode`, `ArrayNode`)

## Pattern — Reader

```java
public final class MyReader {

    private final JsonFactory jsonFactory;

    public MyReader(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public MyRecord read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String fieldA = null;
            Integer fieldB = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();
                switch (fieldName) {
                    case "field_a" -> fieldA = parser.getValueAsString();
                    case "field_b" -> fieldB = parser.getIntValue();
                    default -> parser.skipChildren();
                }
            }

            return MyRecord.builder()
                    .fieldA(fieldA)
                    .fieldB(fieldB)
                    .build();
        }
    }

    private static void expect(JsonToken actual, JsonToken expected) throws IOException {
        if (actual != expected) {
            throw new IOException("Expected " + expected + " but got " + actual);
        }
    }
}
```

## Pattern — Writer

```java
public final class MyWriter {

    private final JsonFactory jsonFactory;

    public MyWriter(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public String write(MyRecord record) throws IOException {
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        try (JsonGenerator generator = jsonFactory.createGenerator(stringWriter)) {
            generator.writeStartObject();
            generator.writeStringField("field_a", record.fieldA());
            generator.writeNumberField("field_b", record.fieldB());
            generator.writeEndObject();
        }
        return stringWriter.toString();
    }
}
```

## Key Rules

- Always use `parser.skipChildren()` in the `default` branch to skip unknown fields.
- Use `parser.currentName()` (not `parser.getText()`) to get the current field name.
- Nested objects/arrays are read by separate `readXxx(parser)` methods in the same reader class.
- Enums are read via their `fromValue()` method (see `java-enum-pattern` skill).

## Nested Objects

Extract nested object reading into private methods that receive the already-opened `JsonParser`:

```java
// In the main read loop:
case "position" -> position = readPosition(parser);

// Private helper — parser is already positioned on START_OBJECT
private ReferencePosition readPosition(JsonParser parser) throws IOException {
    expect(parser.currentToken(), JsonToken.START_OBJECT);

    Integer latitude = null;
    Integer longitude = null;

    while (parser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = parser.currentName();
        parser.nextToken();
        switch (fieldName) {
            case "latitude" -> latitude = parser.getIntValue();
            case "longitude" -> longitude = parser.getIntValue();
            default -> parser.skipChildren();
        }
    }

    return ReferencePosition.builder()
            .latitude(latitude)
            .longitude(longitude)
            .build();
}
```

For arrays, consume `START_ARRAY` / `END_ARRAY` and loop on each element:

```java
private List<WayPoint> readWayPoints(JsonParser parser) throws IOException {
    expect(parser.currentToken(), JsonToken.START_ARRAY);
    List<WayPoint> wayPoints = new ArrayList<>();
    while (parser.nextToken() != JsonToken.END_ARRAY) {
        wayPoints.add(readWayPoint(parser));
    }
    return wayPoints;
}
```

