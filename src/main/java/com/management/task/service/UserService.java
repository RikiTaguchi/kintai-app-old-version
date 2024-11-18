package com.management.task.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.management.task.model.User;
import com.management.task.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> findAll() {
        return this.userRepository.findAll();
    }
    
    public List<User> findByClassAreaId(UUID classAreaId) {
        return this.userRepository.findByClassAreaId(classAreaId);
    }
    
    public void add(User user) {
        userRepository.save(user);
    }

    public void deleteById(UUID userId) {
        userRepository.deleteById(userId);
    }
    
    @Transactional
    public void update(User user) {
        userRepository.update(user.getId(), user.getLoginId(), user.getUserName(), user.getPassword(), user.getClassAreaId());
    }
    
    public User getByUserId(UUID userId) {
        return this.userRepository.getByUserId(userId);
    }
    
    public User getByLoginId(String loginId) {
        return this.userRepository.getByLoginId(loginId);
    }

}
