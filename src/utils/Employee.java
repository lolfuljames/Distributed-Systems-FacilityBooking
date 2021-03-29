package utils;

import java.io.*;
import java.util.*;

public class Employee {
   public String name;
   public int age;
   public String designation;
//   public double salary;
   public ArrayList<String> numbers;

   // This is the constructor of the class Employee
   public Employee() {
	   
   }
   
   public Employee(String name) {
      this.name = name;
      this.designation = "Mr.";
      this.numbers = new ArrayList<String>();
//      this.numbers.add(1);
//      this.numbers.add(2);
//      this.numbers.add(999);
      this.numbers.add("A");
      this.numbers.add("B");
   }

   // Assign the age of the Employee  to the variable age.
   public void empAge(int empAge) {
      age = empAge;
   }

   /* Assign the designation to the variable designation.*/
   public void empDesignation(String empDesig) {
      designation = empDesig;
   }

   /* Assign the salary to the variable	salary.*/
//   public void empSalary(double empSalary) {
//      salary = empSalary;
//   }

   /* Print the Employee details */
   public void printEmployee() {
      System.out.println("Name:"+ name );
      System.out.println("Age:" + age );
      System.out.println("Designation:" + designation );
//      System.out.println("Salary:" + salary);
   }
}