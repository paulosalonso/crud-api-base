package com.alon.spring.crud.resource.dto;

public interface ResourceDtoConverterProvider {
    
    public <C extends OutputDtoConverter> C getListOutputDtoConverter();
                       
    public <D, O, C extends OutputDtoConverter<D, O>> C getReadOutputDtoConverter();
                       
    public <I extends InputDto, D, C extends InputDtoConverter<I, D>> C getCreateInputDtoConverter();
                       
    public <D, O, C extends OutputDtoConverter<D, O>> C getCreateOutputDtoConverter();
                       
    public <I extends InputDto, D, C extends InputDtoConverter<I, D>> C getUpdateInputDtoConverter();
                       
    public <D, O, C extends OutputDtoConverter<D, O>> C getUpdateOutputDtoConverter();
                       
}
