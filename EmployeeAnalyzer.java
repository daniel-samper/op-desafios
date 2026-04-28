import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class EmployeeAnalyzer {

    static class Employee {
        int id;
        String name;
        String surname;
        double wage;
        String area;
    }

    static class Area {
        String code;
        String name;
    }

    static class CompanyData {
        Employee[] employees;
        Area[] areas;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java EmployeeAnalyzer <json_file>");
            System.exit(1);
        }

        String fileName = args[0];
        CompanyData data;

        try (FileReader reader = new FileReader(fileName)) {
            Gson gson = new Gson();
            data = gson.fromJson(reader, CompanyData.class);
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Build area code to name map
        Map<String, String> areaMap = new HashMap<>();
        for (Area area : data.areas) {
            areaMap.put(area.code, area.name);
        }

        // Question 1: Global max, min, and average
        analyzeGlobal(data.employees);

        // Question 2: Area-based max, min, and average
        analyzeByArea(data.employees, areaMap);

        // Question 3: Areas with most and least employees
        analyzeEmployeeCountByArea(data.employees, areaMap);

        // Question 4: Highest wages for each surname
        analyzeBySurname(data.employees);
    }

    private static void analyzeGlobal(Employee[] employees) {
        double maxWage = Double.NEGATIVE_INFINITY;
        double minWage = Double.POSITIVE_INFINITY;
        double totalWage = 0.0;

        for (Employee emp : employees) {
            if (emp.wage > maxWage) {
                maxWage = emp.wage;
            }
            if (emp.wage < minWage) {
                minWage = emp.wage;
            }
            totalWage += emp.wage;
        }

        double avgWage = totalWage / employees.length;

        // Print all employees with max wage
        for (Employee emp : employees) {
            if (emp.wage == maxWage) {
                System.out.printf("global_max|%s %s|%.2f%n", emp.name, emp.surname, emp.wage);
            }
        }

        // Print all employees with min wage
        for (Employee emp : employees) {
            if (emp.wage == minWage) {
                System.out.printf("global_min|%s %s|%.2f%n", emp.name, emp.surname, emp.wage);
            }
        }

        // Print average
        System.out.printf("global_avg|%.2f%n", avgWage);
    }

    private static void analyzeByArea(Employee[] employees, Map<String, String> areaMap) {
        // Group employees by area
        Map<String, List<Employee>> byArea = new HashMap<>();
        for (Employee emp : employees) {
            byArea.computeIfAbsent(emp.area, k -> new ArrayList<>()).add(emp);
        }

        // For each area, compute max, min, avg
        for (Map.Entry<String, List<Employee>> entry : byArea.entrySet()) {
            String areaCode = entry.getKey();
            List<Employee> areaEmployees = entry.getValue();
            String areaName = areaMap.get(areaCode);

            double maxWage = Double.NEGATIVE_INFINITY;
            double minWage = Double.POSITIVE_INFINITY;
            double totalWage = 0.0;

            for (Employee emp : areaEmployees) {
                if (emp.wage > maxWage) {
                    maxWage = emp.wage;
                }
                if (emp.wage < minWage) {
                    minWage = emp.wage;
                }
                totalWage += emp.wage;
            }

            double avgWage = totalWage / areaEmployees.size();

            // Print max employees for this area
            for (Employee emp : areaEmployees) {
                if (emp.wage == maxWage) {
                    System.out.printf("area_max|%s|%s %s|%.2f%n", areaName, emp.name, emp.surname, emp.wage);
                }
            }

            // Print min employees for this area
            for (Employee emp : areaEmployees) {
                if (emp.wage == minWage) {
                    System.out.printf("area_min|%s|%s %s|%.2f%n", areaName, emp.name, emp.surname, emp.wage);
                }
            }

            // Print average for this area
            System.out.printf("area_avg|%s|%.2f%n", areaName, avgWage);
        }
    }

    private static void analyzeEmployeeCountByArea(Employee[] employees, Map<String, String> areaMap) {
        // Count employees per area
        Map<String, Integer> countByArea = new HashMap<>();
        for (Employee emp : employees) {
            countByArea.put(emp.area, countByArea.getOrDefault(emp.area, 0) + 1);
        }

        // Find max and min counts
        int maxCount = Integer.MIN_VALUE;
        int minCount = Integer.MAX_VALUE;

        for (int count : countByArea.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
            if (count < minCount) {
                minCount = count;
            }
        }

        // Collect areas with max and min counts
        List<String> maxAreas = new ArrayList<>();
        List<String> minAreas = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : countByArea.entrySet()) {
            String areaCode = entry.getKey();
            int count = entry.getValue();
            String areaName = areaMap.get(areaCode);

            if (count == maxCount) {
                maxAreas.add(areaName);
            }
            if (count == minCount) {
                minAreas.add(areaName);
            }
        }

        // Sort for consistent output (optional based on problem statement saying no ordering needed)
        // But let's not sort as per requirement "The list does not need to be ordered"

        // Print least_employees first (as in example)
        for (String areaName : minAreas) {
            System.out.printf("least_employees|%s|%d%n", areaName, minCount);
        }

        // Print most_employees
        for (String areaName : maxAreas) {
            System.out.printf("most_employees|%s|%d%n", areaName, maxCount);
        }
    }

    private static void analyzeBySurname(Employee[] employees) {
        // Group employees by surname
        Map<String, List<Employee>> bySurname = new HashMap<>();
        for (Employee emp : employees) {
            bySurname.computeIfAbsent(emp.surname, k -> new ArrayList<>()).add(emp);
        }

        // For each surname with more than one employee, find max wage
        for (Map.Entry<String, List<Employee>> entry : bySurname.entrySet()) {
            String surname = entry.getKey();
            List<Employee> surnameEmployees = entry.getValue();

            // Only process if more than one employee
            if (surnameEmployees.size() <= 1) {
                continue;
            }

            // Find max wage for this surname
            double maxWage = Double.NEGATIVE_INFINITY;
            for (Employee emp : surnameEmployees) {
                if (emp.wage > maxWage) {
                    maxWage = emp.wage;
                }
            }

            // Print all employees with max wage for this surname
            for (Employee emp : surnameEmployees) {
                if (emp.wage == maxWage) {
                    System.out.printf("last_name_max|%s|%s %s|%.2f%n", surname, emp.name, emp.surname, emp.wage);
                }
            }
        }
    }
}
