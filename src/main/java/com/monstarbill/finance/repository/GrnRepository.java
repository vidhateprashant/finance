package com.monstarbill.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.Grn;

public interface GrnRepository extends JpaRepository<Grn, String> {

}
