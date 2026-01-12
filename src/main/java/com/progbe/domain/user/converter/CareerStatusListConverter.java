package com.progbe.domain.user.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.user.type.CareerStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class CareerStatusListConverter implements AttributeConverter<List<CareerStatus>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CareerStatus> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert CareerStatus list to JSON", e);
        }
    }

    @Override
    public List<CareerStatus> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<List<CareerStatus>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to CareerStatus list", e);
        }
    }
}
