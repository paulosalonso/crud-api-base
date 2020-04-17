package com.alon.spring.crud.api.controller.input.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import com.alon.spring.crud.domain.model.Example;

public class ModelMapperInputMapperTest {

    @Test
    public void whenMapThenReturn() {
        ModelMapperInputMapper<EntityTestInput, Example> mapper =
                new ModelMapperInputMapper<>(Example.class);

        EntityTestInput input = new EntityTestInput();
        input.setId(1L);
        input.setProperty("string-property");

        Example output = mapper.map(input);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(1L);
        assertThat(output.getStringProperty()).isEqualTo("string-property");
    }

    @Test
    public void whenInputAndOutputAreEqualsThenReturn() {
        ModelMapperInputMapper<Example, Example> mapper =
                new ModelMapperInputMapper<>(Example.class);

        Example input = new Example();
        input.setId(1L);
        input.setStringProperty("string-property");

        Example output = mapper.map(input);

        assertThat(output)
                .isNotNull()
                .isSameAs(input);
    }

    @Test
    public void whenGetAndConfigureModelMapperThenSuccess() {
        ModelMapperInputMapper<EntityTestInput, Example> mapper =
                new ModelMapperInputMapper<>(Example.class);

        mapper.getModelMapper()
                .getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        EntityTestInput input = new EntityTestInput();
        input.setId(1L);
        input.setProperty("property");

        Example output = mapper.map(input);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(1L);
        assertThat(output.getStringProperty()).isNull();
    }

    @Test
    public void whenProvideConfiguredModelMapperThenSuccess() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        ModelMapperInputMapper<EntityTestInput, Example> mapper =
                new ModelMapperInputMapper<>(Example.class, modelMapper);

        EntityTestInput input = new EntityTestInput();
        input.setId(1L);
        input.setProperty("property");

        Example output = mapper.map(input);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(1L);
        assertThat(output.getStringProperty()).isNull();
    }

    private class EntityTestInput {

        private Long id;
        private String property;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }
}
