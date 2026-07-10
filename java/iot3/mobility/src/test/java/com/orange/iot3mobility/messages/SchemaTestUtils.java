/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility methods for validating serialized JSON against ETSI schema files.
 * <p>
 * Schema files are resolved relative to {@code ../../../schema/} from the
 * Gradle test working directory ({@code java/iot3/mobility/}).
 * <p>
 * Some schemas (MAPEM, SPATEM) use {@code $ref} values that are resolved against
 * the schema's {@code $id} URL (an {@code https://} address) rather than the local
 * file path. The factory is configured with a schema mapper that redirects any
 * remote DSRC schema URI to the local copy on disk.
 */
public final class SchemaTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path SCHEMA_ROOT = Path.of("../../../schema");

    private static final JsonSchemaFactory FACTORY = buildFactory();

    private SchemaTestUtils() {
    }

    private static JsonSchemaFactory buildFactory() {
        String dsrcUri = SCHEMA_ROOT.resolve("dsrc/dsrc_schema_2-0-0.json").toAbsolutePath().toUri().toString();
        return JsonSchemaFactory.builder(
                        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012))
                .schemaMappers(mappers -> mappers.add(iri -> {
                    if (iri.toString().endsWith("dsrc_schema_2-0-0.json")) {
                        return AbsoluteIri.of(dsrcUri);
                    }
                    return null;
                }))
                .build();
    }

    /**
     * Loads a JSON Schema from a path relative to the repository {@code schema/} directory.
     *
     * @param relativePath e.g. {@code "cam/cam_schema_2-4-0.json"}
     */
    public static JsonSchema loadSchema(String relativePath) throws IOException {
        Path schemaPath = SCHEMA_ROOT.resolve(relativePath);
        return FACTORY.getSchema(schemaPath.toUri());
    }

    /**
     * Asserts that the given JSON bytes conform to the provided schema.
     *
     * @param schema the JSON Schema to validate against
     * @param json   the serialized JSON bytes
     */
    public static void assertConformsToSchema(JsonSchema schema, byte[] json) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        Set<ValidationMessage> violations = schema.validate(node);
        assertTrue(violations.isEmpty(),
                "Schema violations: " + violations);
    }
}
