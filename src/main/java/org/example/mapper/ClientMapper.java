package org.example.mapper;


import org.example.dto.LoanStatementRequestDto;
import org.example.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "maritalStatus", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "employment", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    Client toEntity(LoanStatementRequestDto dto);
}
