package com.tka.ExcelTask.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tka.ExcelTask.entity.Student;


public interface StudentRepository extends JpaRepository<Student, Integer> {

	
    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);
	
}
