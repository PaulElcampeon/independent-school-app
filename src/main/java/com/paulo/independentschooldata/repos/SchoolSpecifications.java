package com.paulo.independentschooldata.repos;

import org.springframework.data.jpa.domain.Specification;
import com.paulo.independentschooldata.domain.School;

public class SchoolSpecifications {

    public static Specification<School> nameContains(String name) {
        return (root, query, cb) -> name == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<School> genderEquals(String gender) {
        return (root, query, cb) -> gender == null ? null :
                cb.equal(cb.lower(root.get("genderProfile")), gender.toLowerCase());
    }

    public static Specification<School> religionEquals(String religion) {
        return (root, query, cb) -> religion == null ? null :
                cb.equal(cb.lower(root.get("religiousAffiliation")), religion.toLowerCase());
    }
}
