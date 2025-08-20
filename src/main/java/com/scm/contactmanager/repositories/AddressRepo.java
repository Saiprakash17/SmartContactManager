package com.scm.contactmanager.repositories;

import com.scm.contactmanager.entities.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepo extends JpaRepository<Address, Long> {

    Address findByContactId(Long contactId);

}
