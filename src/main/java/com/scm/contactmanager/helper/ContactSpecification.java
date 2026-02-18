package com.scm.contactmanager.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.Relationship;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.AdvancedSearchCriteria;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Component
public class ContactSpecification {

    public Specification<Contact> buildSpecification(AdvancedSearchCriteria criteria, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user
            predicates.add(cb.equal(root.get("user"), user));

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    "%" + criteria.getName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getEmail())) {
                predicates.add(cb.like(
                    cb.lower(root.get("email")),
                    "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getPhoneNumber())) {
                predicates.add(cb.like(
                    root.get("phoneNumber"),
                    "%" + criteria.getPhoneNumber() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getRelationship())) {
                try {
                    Relationship relationship = Relationship.valueOf(criteria.getRelationship().toUpperCase());
                    predicates.add(cb.equal(root.get("relationship"), relationship));
                } catch (IllegalArgumentException e) {
                    // If the relationship value is not a valid enum, ignore it
                }
            }

            if (criteria.getIsFavorite() != null) {
                predicates.add(cb.equal(root.get("favorite"), criteria.getIsFavorite()));
            }

            if (StringUtils.hasText(criteria.getWebsiteUrl())) {
                predicates.add(cb.like(
                    cb.lower(root.get("website")),
                    "%" + criteria.getWebsiteUrl().toLowerCase() + "%"
                ));
            }

            if (criteria.getBirthdateFrom() != null || criteria.getBirthdateTo() != null) {
                Join<Contact, Address> addressJoin = root.join("address", JoinType.LEFT);
                
                if (criteria.getBirthdateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        addressJoin.get("birthdate"),
                        criteria.getBirthdateFrom()
                    ));
                }

                if (criteria.getBirthdateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        addressJoin.get("birthdate"),
                        criteria.getBirthdateTo()
                    ));
                }
            }

            if (StringUtils.hasText(criteria.getCity())) {
                Join<Contact, Address> addressJoin = root.join("address", JoinType.LEFT);
                predicates.add(cb.like(
                    cb.lower(addressJoin.get("city")),
                    "%" + criteria.getCity().toLowerCase() + "%"
                ));
            }

            if (!CollectionUtils.isEmpty(criteria.getTagIds())) {
                Join<Contact, ContactTag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(criteria.getTagIds()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
