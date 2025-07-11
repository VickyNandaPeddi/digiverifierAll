package com.aashdit.digiverifier.config.superadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aashdit.digiverifier.config.superadmin.model.Source;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    Source findBySourceName(String sourceName);

    List<Source> findByIsActiveTrue();

    @Query(value = "SELECT * FROM t_dgv_source tds WHERE tds.source_name LIKE %:sourceName%", nativeQuery = true)
    List<Source> findBySourceNameList(@Param("sourceName") String sourceName);


}
