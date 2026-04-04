package com.mylibrary.repository;

import com.mylibrary.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
