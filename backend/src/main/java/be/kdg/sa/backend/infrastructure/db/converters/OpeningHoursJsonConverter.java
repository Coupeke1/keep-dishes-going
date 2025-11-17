package be.kdg.sa.backend.infrastructure.db.converters;

import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OpeningHoursJsonConverter implements AttributeConverter<OpeningHours, String> {

    private final ObjectMapper objectMapper;

    public OpeningHoursJsonConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule());
    }

    @Override
    public String convertToDatabaseColumn(OpeningHours attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializing OpeningHours to JSON", e);
        }
    }

    @Override
    public OpeningHours convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return objectMapper.readValue(dbData, OpeningHours.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error deserializing OpeningHours from JSON", e);
        }
    }
}
