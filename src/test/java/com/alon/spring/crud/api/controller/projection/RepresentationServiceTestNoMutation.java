package com.alon.spring.crud.api.controller.projection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.alon.spring.crud.api.controller.output.RepresentationTestDTO;
import com.alon.spring.crud.api.projection.RepresentationService;

public class RepresentationServiceTestNoMutation {

    private RepresentationService representationService = new RepresentationService();

    @Test
    public void whenGetRepresentationThenSuccess() {
        Map<String, Object> representation =
                representationService.getRepresentationOf(RepresentationTestDTO.class);

        assertThat(representation)
                .isNotNull()
                .hasSize(6)
                .containsOnlyKeys("id", "dto", "stringList[]",
                        "stringArray[]", "objectArray[]", "objectList[]");

        assertThat(representation.get("id")).isEqualTo("long");
        assertThat(representation.get("stringList[]")).isEqualTo("string");
        assertThat(representation.get("stringArray[]")).isEqualTo("string");
        assertThat((Map) representation.get("dto"))
                .hasSize(2)
                .containsOnlyKeys("id", "property")
                .containsValues("long", "string");
        assertThat((Map) representation.get("objectArray[]"))
                .hasSize(2)
                .containsOnlyKeys("id", "property")
                .containsValues("long", "string");
        assertThat((Map) representation.get("objectList[]"))
                .hasSize(2)
                .containsOnlyKeys("id", "property")
                .containsValues("long", "string");
    }
}
