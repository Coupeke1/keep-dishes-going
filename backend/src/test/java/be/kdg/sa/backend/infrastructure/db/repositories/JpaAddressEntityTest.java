package be.kdg.sa.backend.infrastructure.db.repositories;

import be.kdg.sa.backend.domain.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JpaAddressEntityTest {

    @Test
    void fromDomain_shouldGenerateNewUUIDEachTime() {
        // Given
        Address address = new Address("Street", "1", null, "1000", "City", "Country");

        // When
        JpaAddressEntity entity1 = JpaAddressEntity.fromDomain(address);
        JpaAddressEntity entity2 = JpaAddressEntity.fromDomain(address);

        // Then
        assertThat(entity1.getId()).isNotNull();
        assertThat(entity2.getId()).isNotNull();
        assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
    }

    @Test
    void fromDomain_shouldReturnNullWhenAddressIsNull() {
        // When
        JpaAddressEntity entity = JpaAddressEntity.fromDomain(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void fromDomain_shouldCopyAllAddressFields() {
        // Given
        Address address = new Address("Main Street", "123", "B", "1000", "Brussels", "Belgium");

        // When
        JpaAddressEntity entity = JpaAddressEntity.fromDomain(address);

        // Then
        assertThat(entity.getStreet()).isEqualTo("Main Street");
        assertThat(entity.getHouseNumber()).isEqualTo("123");
        assertThat(entity.getBusNumber()).isEqualTo("B");
        assertThat(entity.getPostalCode()).isEqualTo("1000");
        assertThat(entity.getCity()).isEqualTo("Brussels");
        assertThat(entity.getCountry()).isEqualTo("Belgium");
    }

    @Test
    void fromDomain_shouldHandleNullBusNumber() {
        // Given
        Address address = new Address("Street", "1", null, "1000", "City", "Country");

        // When
        JpaAddressEntity entity = JpaAddressEntity.fromDomain(address);

        // Then
        assertThat(entity.getBusNumber()).isNull();
    }

    @Test
    void updateFromDomain_shouldThrowExceptionWhenAddressIsNull() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );

        // When & Then
        assertThatThrownBy(() -> entity.updateFromDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Address is required");
    }

    @Test
    void updateFromDomain_shouldUpdateAllFields() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Old Street", "1", "A", "1000", "Old City", "Old Country"
        );
        Address newAddress = new Address("New Street", "2", "B", "2000", "New City", "New Country");

        // When
        entity.updateFromDomain(newAddress);

        // Then
        assertThat(entity.getStreet()).isEqualTo("New Street");
        assertThat(entity.getHouseNumber()).isEqualTo("2");
        assertThat(entity.getBusNumber()).isEqualTo("B");
        assertThat(entity.getPostalCode()).isEqualTo("2000");
        assertThat(entity.getCity()).isEqualTo("New City");
        assertThat(entity.getCountry()).isEqualTo("New Country");
    }

    @Test
    void updateFromDomain_shouldUpdateBusNumberToNull() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", "A", "1000", "City", "Country"
        );
        Address newAddress = new Address("Street", "1", null, "1000", "City", "Country");

        // When
        entity.updateFromDomain(newAddress);

        // Then
        assertThat(entity.getBusNumber()).isNull();
    }

    @Test
    void updateFromDomain_shouldUpdateBusNumberFromNullToValue() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        Address newAddress = new Address("Street", "1", "B", "1000", "City", "Country");

        // When
        entity.updateFromDomain(newAddress);

        // Then
        assertThat(entity.getBusNumber()).isEqualTo("B");
    }

    @Test
    void updateAddress_shouldUpdateAllFields() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Old Street", "1", "A", "1000", "Old City", "Old Country"
        );

        // When
        entity.updateAddress("New Street", "2", "B", "2000", "New City", "New Country");

        // Then
        assertThat(entity.getStreet()).isEqualTo("New Street");
        assertThat(entity.getHouseNumber()).isEqualTo("2");
        assertThat(entity.getBusNumber()).isEqualTo("B");
        assertThat(entity.getPostalCode()).isEqualTo("2000");
        assertThat(entity.getCity()).isEqualTo("New City");
        assertThat(entity.getCountry()).isEqualTo("New Country");
    }

    @Test
    void updateAddress_shouldHandleNullBusNumber() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", "A", "1000", "City", "Country"
        );

        // When
        entity.updateAddress("Street", "1", null, "1000", "City", "Country");

        // Then
        assertThat(entity.getBusNumber()).isNull();
    }

    @Test
    void toDomain_shouldConvertToAddressWithSameValues() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Main Street", "123", "B", "1000", "Brussels", "Belgium"
        );

        // When
        Address address = entity.toDomain();

        // Then
        assertThat(address.street()).isEqualTo("Main Street");
        assertThat(address.houseNumber()).isEqualTo("123");
        assertThat(address.busNumber()).isEqualTo("B");
        assertThat(address.postalCode()).isEqualTo("1000");
        assertThat(address.city()).isEqualTo("Brussels");
        assertThat(address.country()).isEqualTo("Belgium");
    }

    @Test
    void toDomain_shouldHandleNullBusNumber() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );

        // When
        Address address = entity.toDomain();

        // Then
        assertThat(address.busNumber()).isNull();
    }

    @Test
    void constructor_shouldAcceptValidFourDigitPostalCode() {
        // Given & When - Should not throw exception
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );

        // Then
        assertThat(entity.getPostalCode()).isEqualTo("1000");
    }

    @Test
    void constructor_shouldAcceptNumericPostalCode() {
        // Given & When - Should not throw exception for numeric postal codes
        JpaAddressEntity entity1 = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1234", "City", "Country"
        );
        JpaAddressEntity entity2 = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "9999", "City", "Country"
        );

        // Then
        assertThat(entity1.getPostalCode()).isEqualTo("1234");
        assertThat(entity2.getPostalCode()).isEqualTo("9999");
    }

    @Test
    void updateAddress_shouldAcceptValidFourDigitPostalCode() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );

        // When - Should not throw exception
        entity.updateAddress("New Street", "2", null, "2000", "New City", "New Country");

        // Then
        assertThat(entity.getPostalCode()).isEqualTo("2000");
    }

    @Test
    void updateFromDomain_shouldAcceptValidFourDigitPostalCode() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        Address newAddress = new Address("New Street", "2", null, "3000", "New City", "New Country");

        // When - Should not throw exception
        entity.updateFromDomain(newAddress);

        // Then
        assertThat(entity.getPostalCode()).isEqualTo("3000");
    }

    @Test
    void defaultConstructor_shouldExistForJpa() {
        // When
        JpaAddressEntity entity = new JpaAddressEntity();

        // Then - Should not throw exception and entity should be created
        assertThat(entity).isNotNull();
    }

    @Test
    void parameterizedConstructor_shouldSetAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        String street = "Test Street";
        String houseNumber = "42";
        String busNumber = "C";
        String postalCode = "1234";
        String city = "Test City";
        String country = "Test Country";

        // When
        JpaAddressEntity entity = new JpaAddressEntity(
                id, street, houseNumber, busNumber, postalCode, city, country
        );

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getStreet()).isEqualTo(street);
        assertThat(entity.getHouseNumber()).isEqualTo(houseNumber);
        assertThat(entity.getBusNumber()).isEqualTo(busNumber);
        assertThat(entity.getPostalCode()).isEqualTo(postalCode);
        assertThat(entity.getCity()).isEqualTo(city);
        assertThat(entity.getCountry()).isEqualTo(country);
    }

    @Test
    void toDomain_and_fromDomain_shouldBeReversible() {
        // Given
        Address originalAddress = new Address("Street", "1", "A", "1000", "City", "Country");

        // When
        JpaAddressEntity entity = JpaAddressEntity.fromDomain(originalAddress);
        Address convertedAddress = entity.toDomain();

        // Then - The converted address should match the original (except for ID which is not in Address)
        assertThat(convertedAddress.street()).isEqualTo(originalAddress.street());
        assertThat(convertedAddress.houseNumber()).isEqualTo(originalAddress.houseNumber());
        assertThat(convertedAddress.busNumber()).isEqualTo(originalAddress.busNumber());
        assertThat(convertedAddress.postalCode()).isEqualTo(originalAddress.postalCode());
        assertThat(convertedAddress.city()).isEqualTo(originalAddress.city());
        assertThat(convertedAddress.country()).isEqualTo(originalAddress.country());
    }

    @Test
    void updateAddress_and_toDomain_shouldWorkTogether() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Old Street", "1", "A", "1000", "Old City", "Old Country"
        );

        // When
        entity.updateAddress("New Street", "2", "B", "2000", "New City", "New Country");
        Address address = entity.toDomain();

        // Then
        assertThat(address.street()).isEqualTo("New Street");
        assertThat(address.houseNumber()).isEqualTo("2");
        assertThat(address.busNumber()).isEqualTo("B");
        assertThat(address.postalCode()).isEqualTo("2000");
        assertThat(address.city()).isEqualTo("New City");
        assertThat(address.country()).isEqualTo("New Country");
    }

    @Test
    void updateFromDomain_and_toDomain_shouldWorkTogether() {
        // Given
        JpaAddressEntity entity = new JpaAddressEntity(
                UUID.randomUUID(), "Old Street", "1", "A", "1000", "Old City", "Old Country"
        );
        Address newAddress = new Address("New Street", "2", "B", "2000", "New City", "New Country");

        // When
        entity.updateFromDomain(newAddress);
        Address convertedAddress = entity.toDomain();

        // Then
        assertThat(convertedAddress.street()).isEqualTo("New Street");
        assertThat(convertedAddress.houseNumber()).isEqualTo("2");
        assertThat(convertedAddress.busNumber()).isEqualTo("B");
        assertThat(convertedAddress.postalCode()).isEqualTo("2000");
        assertThat(convertedAddress.city()).isEqualTo("New City");
        assertThat(convertedAddress.country()).isEqualTo("New Country");
    }
}