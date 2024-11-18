package com.management.task.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.management.task.model.Manager;


public interface ManagerRepository extends JpaRepository<Manager, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM managers WHERE id = :id LIMIT 1")
    Manager getById(@Param("id") UUID id);
    
    @Query(nativeQuery = true, value = "SELECT * FROM managers WHERE loginid = :loginid LIMIT 1")
    Manager getByLoginId(@Param("loginid") String loginid);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE managers SET loginid = :loginid, password = :password, classarea = :classarea WHERE id = :id")
    void update(@Param("id") UUID id, @Param("loginid") String loginid, @Param("password") String password, @Param("classarea") String classarea);

}
