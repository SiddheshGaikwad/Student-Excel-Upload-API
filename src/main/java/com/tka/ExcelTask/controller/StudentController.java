package com.tka.ExcelTask.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tka.ExcelTask.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

	@Autowired
	private StudentService studentService;

	@PostMapping("/upload-excel")
	public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {

		try {

			Map<String, Object> response = studentService.uploadExcel(file);

			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (RuntimeException e) {

			Map<String, Object> error = Map.of("message", e.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}
}