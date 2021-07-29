package com.rav.bhaj.drools.model;

import java.io.Serializable;

	public class Student implements Serializable {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String studentId;
		private String studentName;
		private String passGrade;
		private int marksObtained;
		private String school;
		public Student() {
		}
		public Student(String id, String name, String passGrade, int marksObtained, String school) {
			super();
			this.studentId = id;
			this.studentName = name;
			this.passGrade = passGrade;
			this.marksObtained = marksObtained;
			this.school = school;
		}


		public String getStudentId() {
			return studentId;
		}
		public void setStudentId(String studentId) {
			this.studentId = studentId;
		}
		public String getStudentName() {
			return studentName;
		}
		public void setStudentName(String studentName) {
			this.studentName = studentName;
		}
		public String getPassGrade() {
			return passGrade;
		}
		public void setPassGrade(String passGrade) {
			this.passGrade = passGrade;
		}
		public int getMarksObtained() {
			return marksObtained;
		}
		public void setMarksObtained(int marksObtained) {
			this.marksObtained = marksObtained;
		}
		public String getSchool() {
			return school;
		}
		public void setSchool(String school) {
			this.school = school;
		}
		@Override
		public String toString() {
			return "Student [id=" + studentId + ", name=" + studentName + ", passGrade=" + passGrade + ", marksObtained=" + marksObtained
					+ ", school=" + school + "]";
		}

	}