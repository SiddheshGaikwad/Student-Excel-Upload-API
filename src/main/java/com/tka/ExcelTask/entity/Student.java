package com.tka.ExcelTask.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name")
    private String studentName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String mobile;

    private String course;

    private String city;

    private BigDecimal fees;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}