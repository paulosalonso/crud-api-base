package com.alon.spring.crud.api.configuration;

import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentSet;
import org.modelmapper.Condition;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper lazyIgnoreModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setPropertyCondition(buildCondition());

        return modelMapper;
    }

    private Condition buildCondition() {
        return context -> {
            if (context.getSourceType().equals(PersistentSet.class))
                return ((PersistentSet) context.getSource()).wasInitialized();

            if (context.getSourceType().equals(PersistentList.class))
                return ((PersistentList) context.getSource()).wasInitialized();

            return true;
        };
    }
}
