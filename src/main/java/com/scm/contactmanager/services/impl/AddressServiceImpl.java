package com.scm.contactmanager.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.repositories.AddressRepo;
import com.scm.contactmanager.services.AddressService;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepo addressRepo;

    @Override
    public Address saveAddress(Address address) {
        return addressRepo.save(address);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    @Override
    public void deleteAddress(Long id) {
        Address address = addressRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
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
    public Address getAddressByContactId(Long contactId) {
        return addressRepo.findByContactId(contactId);
    }

}
