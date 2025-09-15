package com.scm.contactmanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.repositories.AddressRepo;
import com.scm.contactmanager.services.impl.AddressServiceImpl;
import com.scm.contactmanager.helper.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepo addressRepo;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address testAddress;

    @BeforeEach
    void setUp() {
        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setStreet("123 Test St");
        testAddress.setCity("Test City");
        testAddress.setState("Test State");
        testAddress.setZipCode("12345");
        testAddress.setCountry("Test Country");
    }

    @Test
    void shouldSaveAddress() {
        when(addressRepo.save(any(Address.class))).thenReturn(testAddress);

        Address savedAddress = addressService.saveAddress(testAddress);

        assertNotNull(savedAddress);
        assertEquals(testAddress.getStreet(), savedAddress.getStreet());
        assertEquals(testAddress.getCity(), savedAddress.getCity());
        verify(addressRepo).save(any(Address.class));
    }

    @Test
    void shouldGetAddressById() {
        when(addressRepo.findById(testAddress.getId())).thenReturn(java.util.Optional.of(testAddress));

        Address foundAddress = addressService.getAddressById(testAddress.getId());

        assertNotNull(foundAddress);
        assertEquals(testAddress.getId(), foundAddress.getId());
        assertEquals(testAddress.getStreet(), foundAddress.getStreet());
        verify(addressRepo).findById(testAddress.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddressNotFound() {
        when(addressRepo.findById(anyLong())).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            addressService.getAddressById(999L);
        });
    }

    @Test
    void shouldDeleteAddress() {
        when(addressRepo.findById(testAddress.getId())).thenReturn(java.util.Optional.of(testAddress));
        doNothing().when(addressRepo).delete(testAddress);

        addressService.deleteAddress(testAddress.getId());

        verify(addressRepo).findById(testAddress.getId());
        verify(addressRepo).delete(testAddress);
    }

    @Test
    void shouldUpdateAddress() {
        Address updatedAddress = new Address();
        updatedAddress.setId(testAddress.getId());
        updatedAddress.setStreet("456 Updated St");
        updatedAddress.setCity("Updated City");
        updatedAddress.setState("Updated State");
        updatedAddress.setZipCode("67890");
        updatedAddress.setCountry("Updated Country");

        when(addressRepo.findById(testAddress.getId())).thenReturn(java.util.Optional.of(testAddress));
        when(addressRepo.save(any(Address.class))).thenReturn(updatedAddress);

        Address result = addressService.updateAddress(updatedAddress);

        assertNotNull(result);
        assertEquals("456 Updated St", result.getStreet());
        assertEquals("Updated City", result.getCity());
    }

    @Test
    void shouldGetAddressByContactId() {
        Long contactId = 1L;
        when(addressRepo.findByContactId(contactId)).thenReturn(testAddress);

        Address result = addressService.getAddressByContactId(contactId);

        assertNotNull(result);
        assertEquals(testAddress.getStreet(), result.getStreet());
        assertEquals(testAddress.getCity(), result.getCity());
        verify(addressRepo).findByContactId(contactId);
    }
}
