package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.Address;

public interface AddressService {

    //save address
    Address saveAddress(Address address);

    //get address by id
    Address getAddressById(Long id);

    //delete address
    void deleteAddress(Long id);

    //update address
    Address updateAddress(Address address);

    //get by contact id
    Address getAddressByContactId(Long contactId);

}
