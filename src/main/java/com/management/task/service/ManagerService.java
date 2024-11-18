package com.management.task.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.management.task.model.Manager;
import com.management.task.repository.ManagerRepository;

import jakarta.transaction.Transactional;

@Service
public class ManagerService {
    
    private final ManagerRepository managerRepository;
    
    @Autowired
    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }
    
    public List<Manager> findAll() {
        return this.managerRepository.findAll();
    }
    
    public Manager getById(UUID managerId) {
        return this.managerRepository.getById(managerId);
    }
    
    public void add(String loginId, String password, String classArea) {
        Manager manager = new Manager();
        manager.setLoginId(loginId);
        manager.setPassword(password);
        manager.setClassArea(classArea);
        managerRepository.save(manager);
    }
    
    public Manager getByLoginId(String loginId) {
        return this.managerRepository.getByLoginId(loginId);
    }

    @Transactional
    public void update(Manager manager) {
        managerRepository.update(manager.getId(), manager.getLoginId(), manager.getPassword(), manager.getClassArea());
    }

    public void deleteById(Manager manager) {
        managerRepository.deleteById(manager.getId());
    }

}
