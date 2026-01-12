package com.progbe.domain.user.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.user.type.JobRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class JobRoleListConverter implements AttributeConverter<List<JobRole>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<JobRole> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JobRole list to JSON", e);
        }
    }

    @Override
    public List<JobRole> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to JobRole list", e);
        }
    }
}
