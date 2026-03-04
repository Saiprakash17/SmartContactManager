package com.scm.contactmanager.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.repositories.AddressRepo;
import com.scm.contactmanager.services.AddressService;

@Service
public class AddressServiceImpl implements AddressService{

    private final AddressRepo addressRepo;

    public AddressServiceImpl(AddressRepo addressRepo) {
        this.addressRepo = addressRepo;
    }

    @Override
    public Address saveAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        return addressRepo.save(address);
    }

    @Override
    public Address getAddressById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        return addressRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    @Override
    public void deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        Address address = addressRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        addressRepo.delete(address);
    }

    @Override
    public Address updateAddress(Address address) {
        Address existingAddress = addressRepo.findById(address.getId()).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + address.getId()));
        existingAddress.setStreet(address.getStreet());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());
        existingAddress.setZipCode(address.getZipCode());
        existingAddress.setCountry(address.getCountry());
        return addressRepo.save(existingAddress);
    }

    @Override
    @Nullable
    public Address getAddressByContactId(Long contactId) {
        return addressRepo.findByContactId(contactId);
    }

}
