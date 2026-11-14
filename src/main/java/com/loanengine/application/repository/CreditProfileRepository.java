
package com.loanengine.application.repository;

import com.loanengine.application.entity.CreditProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditProfileRepository
        extends JpaRepository<CreditProfile, Long> {

    List<CreditProfile> findByCustomerId(Long customerId);

    Optional<CreditProfile> findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(
            Long customerId
    );
}
