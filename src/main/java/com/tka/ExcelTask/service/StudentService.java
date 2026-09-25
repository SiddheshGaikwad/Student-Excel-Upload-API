package com.tka.ExcelTask.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tka.ExcelTask.entity.Student;
import com.tka.ExcelTask.repository.StudentRepository;

@Service
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;

	public Map<String, Object> uploadExcel(MultipartFile file) {

		Map<String, Object> response = new HashMap<>();

		// ---------------- FILE VALIDATION ----------------

		if (file == null || file.isEmpty()) {
			throw new RuntimeException("File is missing or empty");
		}

		String fileName = file.getOriginalFilename();

		if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {

			throw new RuntimeException("Only .xlsx files are allowed");
		}

		// ---------------- RESPONSE DATA ----------------

		int totalRows = 0;
		int insertedCount = 0;
		int failedCount = 0;

		List<Map<String, Object>> errorsList = new ArrayList<>();

		// Store emails/mobile numbers from current Excel
		Set<String> excelEmails = new HashSet<>();
		Set<String> excelMobiles = new HashSet<>();

		try {

			Workbook workbook = new XSSFWorkbook(file.getInputStream());

			Sheet sheet = workbook.getSheetAt(0);

			DataFormatter formatter = new DataFormatter();

			/*
			 * Iterator starts from first row. Row 0 = Excel header.
			 */
			Iterator<Row> rowIterator = sheet.iterator();

			// Skip header row
			if (rowIterator.hasNext()) {
				rowIterator.next();
			}

			while (rowIterator.hasNext()) {

				Row row = rowIterator.next();

				/*
				 * Excel row number: Java index starts at 0 Excel row starts at 1
				 *
				 * Therefore +1
				 */
				int rowNumber = row.getRowNum() + 1;

				// Ignore completely empty rows
				if (isEmptyRow(row, formatter)) {
					continue;
				}

				totalRows++;

				// ---------------- READ EXCEL VALUES ----------------

				String studentName = getCellValue(row, 0, formatter);
				String email = getCellValue(row, 1, formatter);
				String mobile = getCellValue(row, 2, formatter);
				String course = getCellValue(row, 3, formatter);
				String city = getCellValue(row, 4, formatter);
				String feesValue = getCellValue(row, 5, formatter);

				// ---------------- VALIDATION ----------------

				List<String> rowErrors = new ArrayList<>();

				// Student Name
				if (studentName == null || studentName.trim().isEmpty()) {

					rowErrors.add("Student name is mandatory");

				} else if (studentName.trim().length() < 3) {

					rowErrors.add("Student name must contain minimum 3 characters");
				}

				// Email
				if (email == null || email.trim().isEmpty()) {

					rowErrors.add("Email is mandatory");

				} else {

					email = email.trim().toLowerCase();

					if (!isValidEmail(email)) {

						rowErrors.add("Invalid email format");

					} else {

						// Check duplicate inside Excel
						if (excelEmails.contains(email)) {

							rowErrors.add("Email is duplicated within Excel file");

						}

						// Check database
						if (studentRepository.existsByEmail(email)) {

							rowErrors.add("Email already exists in database");
						}
					}
				}

				// Mobile
				if (mobile == null || mobile.trim().isEmpty()) {

					rowErrors.add("Mobile number is mandatory");

				} else {

					mobile = mobile.trim();

					if (!mobile.matches("\\d{10}")) {

						rowErrors.add("Mobile number must contain exactly 10 digits");

					} else {

						// Check duplicate inside Excel
						if (excelMobiles.contains(mobile)) {

							rowErrors.add("Mobile number is duplicated within Excel file");
						}

						// Check database
						if (studentRepository.existsByMobile(mobile)) {

							rowErrors.add("Mobile number already exists in database");
						}
					}
				}

				// Course
				if (course == null || course.trim().isEmpty()) {

					rowErrors.add("Course is mandatory");

				} else {

					course = course.trim();

					if (!(course.equalsIgnoreCase("Java") || course.equalsIgnoreCase("Python")
							|| course.equalsIgnoreCase("Testing") || course.equalsIgnoreCase("Data Analytics"))) {

						rowErrors.add("Course must be Java, Python, Testing, or Data Analytics");
					}
				}

				// City
				if (city == null || city.trim().isEmpty()) {

					rowErrors.add("City is mandatory");
				}

				// Fees
				BigDecimal fees = null;

				if (feesValue == null || feesValue.trim().isEmpty()) {

					rowErrors.add("Fees is mandatory");

				} else {

					try {

						fees = new BigDecimal(feesValue.trim());

						if (fees.compareTo(BigDecimal.ZERO) <= 0) {

							rowErrors.add("Fees must be greater than 0");
						}

					} catch (NumberFormatException e) {

						rowErrors.add("Fees must be numeric");
					}
				}

				// ---------------- CHECK ROW RESULT ----------------

				if (!rowErrors.isEmpty()) {

					/*
					 * Row contains errors.
					 *
					 * Do NOT save it.
					 */

					failedCount++;

					Map<String, Object> errorMap = new HashMap<>();

					errorMap.put("row", rowNumber);
					errorMap.put("email", email);
					errorMap.put("mobile", mobile);
					errorMap.put("errors", rowErrors);

					errorsList.add(errorMap);

				} else {

					// ---------------- SAVE VALID ROW ----------------

					Student student = new Student();

					student.setStudentName(studentName.trim());
					student.setEmail(email);
					student.setMobile(mobile);
					student.setCourse(course);
					student.setCity(city.trim());
					student.setFees(fees);
					student.setCreatedAt(LocalDateTime.now());

					studentRepository.save(student);

					insertedCount++;

					/*
					 * Add email/mobile only after successful validation.
					 *
					 * This allows us to detect duplicates in subsequent Excel rows.
					 */
					excelEmails.add(email);
					excelMobiles.add(mobile);
				}
			}

			workbook.close();

		} catch (Exception e) {

			throw new RuntimeException("Unable to process Excel file: " + e.getMessage());
		}

		// ---------------- FINAL RESPONSE ----------------

		response.put("message", "Excel processing completed");

		response.put("total_rows", totalRows);

		response.put("inserted_count", insertedCount);

		response.put("failed_count", failedCount);

		response.put("errors", errorsList);

		return response;
	}

	// =====================================================
	// GET CELL VALUE
	// =====================================================

	private String getCellValue(Row row, int columnNumber, DataFormatter formatter) {

		Cell cell = row.getCell(columnNumber);

		if (cell == null) {
			return "";
		}

		return formatter.formatCellValue(cell).trim();
	}

	// =====================================================
	// CHECK EMPTY ROW
	// =====================================================

	private boolean isEmptyRow(Row row, DataFormatter formatter) {

		for (int i = 0; i < 6; i++) {

			String value = getCellValue(row, i, formatter);

			if (!value.trim().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	// =====================================================
	// EMAIL VALIDATION
	// =====================================================

	private boolean isValidEmail(String email) {

		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}
}