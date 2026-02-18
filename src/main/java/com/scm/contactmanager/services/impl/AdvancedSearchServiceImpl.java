package com.scm.contactmanager.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.ContactSpecification;
import com.scm.contactmanager.payloads.AdvancedSearchCriteria;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.AdvancedSearchService;

@Service
@Transactional(readOnly = true)
public class AdvancedSearchServiceImpl implements AdvancedSearchService {

    @Autowired
    private ContactRepo contactRepository;

    @Autowired
    private ContactSpecification contactSpecification;

    @Override
    public Page<Contact> search(AdvancedSearchCriteria criteria, User user) {
        Specification<Contact> spec = contactSpecification.buildSpecification(criteria, user);

        Sort.Direction direction = criteria.getSortDirection().equalsIgnoreCase("ASC")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, criteria.getSortBy());

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return contactRepository.findAll(spec, pageable);
    }
}
