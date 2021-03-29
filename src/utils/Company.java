package utils;

import java.util.ArrayList;

public class Company {
	
	private ArrayList<Employee> employees;

	public Company() {
		// TODO Auto-generated constructor stub
		employees = new ArrayList<Employee>();
		employees.add(new Employee("TJL"));
		employees.add(new Employee("TJX"));
		employees.add(new Employee("TJE"));
	}
	
	public String getEmployeeNames() {
		String results = new String("");
		for(Employee emp: employees) {
			results = results + emp.name + " ";
		}
		
		return results;
	}

}
