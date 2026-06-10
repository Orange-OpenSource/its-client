# Skill: Java Testing Conventions

## Frameworks

Use **JUnit Jupiter 5** and **Mockito** only.

## Forbidden

Do not introduce any of the following libraries:
- AssertJ
- Hamcrest
- TestNG
- Any other assertion or mocking library

## Assertions

Use JUnit 5 built-in assertions:

```java
import static org.junit.jupiter.api.Assertions.*;

assertEquals(expected, actual);
assertNotNull(value);
assertTrue(condition);
assertThrows(IllegalStateException.class, () -> builder.build());
```

## Mocking

Use Mockito for mocking dependencies:

```java
import static org.mockito.Mockito.*;

MyDependency dependency = mock(MyDependency.class);
when(dependency.getValue()).thenReturn("test");
verify(dependency).getValue();
```

## Test Class Structure

Test method names follow the pattern `methodUnderTest_scenario_expectedBehavior`:

```java
class MyRecordTest {

    @Test
    void build_withAllFields_shouldCreateRecord() {
        // arrange / act / assert
    }

    @Test
    void build_withMissingField_shouldThrow() {
        assertThrows(IllegalStateException.class, () ->
                MyRecord.builder().build());
    }
}
```

## Codec Round-Trip Tests

For message codecs, verify that `read → write → read` produces an identical result:

```java
@Test
void readThenWrite_shouldRoundTrip() throws IOException {
    String originalJson = loadResource("my_message.json");

    MyEnvelope envelope = MyCodec.read(originalJson);
    String writtenJson = MyCodec.write(envelope);
    MyEnvelope reRead = MyCodec.read(writtenJson);

    assertEquals(envelope, reRead);
}
```

