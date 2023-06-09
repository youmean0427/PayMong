package com.paymong.common.common.repository;

import com.paymong.common.common.entity.GroupCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupCodeRepository extends JpaRepository<GroupCode, String> {

    Optional<GroupCode> findByCode(String code);
}
