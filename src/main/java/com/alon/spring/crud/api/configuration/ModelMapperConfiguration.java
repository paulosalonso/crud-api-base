package com.alon.spring.crud.api.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hibernate.Hibernate.isInitialized;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper lazyIgnoreModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setPropertyCondition(context -> isInitialized(context.getSource()));

        return modelMapper;
    }
}
