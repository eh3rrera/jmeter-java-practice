package com.example.employeeapi.database;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

/**
 * DataGenerator provides utilities for generating large amounts of test employee data
 * for performance testing purposes. Uses pseudo-random selection from predefined pools
 * to create realistic but varied employee records.
 */
public class DataGenerator {

    // First names pool (~100 names)
    private static final String[] FIRST_NAMES = {
        "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
        "William", "Barbara", "David", "Elizabeth", "Richard", "Susan", "Joseph", "Jessica",
        "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
        "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
        "Steven", "Kimberly", "Andrew", "Emily", "Paul", "Donna", "Joshua", "Michelle",
        "Kenneth", "Carol", "Kevin", "Amanda", "Brian", "Dorothy", "George", "Melissa",
        "Timothy", "Deborah", "Ronald", "Stephanie", "Edward", "Rebecca", "Jason", "Sharon",
        "Jeffrey", "Laura", "Ryan", "Cynthia", "Jacob", "Kathleen", "Gary", "Amy",
        "Nicholas", "Angela", "Eric", "Shirley", "Jonathan", "Anna", "Stephen", "Brenda",
        "Larry", "Pamela", "Justin", "Emma", "Scott", "Nicole", "Brandon", "Helen",
        "Benjamin", "Samantha", "Samuel", "Katherine", "Raymond", "Christine", "Gregory", "Debra",
        "Frank", "Rachel", "Alexander", "Carolyn", "Patrick", "Janet", "Jack", "Catherine",
        "Dennis", "Maria", "Jerry", "Heather"
    };

    // Last names pool (~100 names)
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
        "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
        "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
        "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
        "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
        "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
        "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey",
        "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson",
        "Watson", "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza",
        "Ruiz", "Hughes", "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers",
        "Long", "Ross", "Foster", "Jimenez"
    };

    // Street names
    private static final String[] STREET_NAMES = {
        "Main", "Oak", "Maple", "Cedar", "Elm", "Pine", "Washington", "Park",
        "Lake", "Hill", "Sunset", "River", "Spring", "Forest", "Mountain", "Valley",
        "Church", "School", "College", "University", "Lincoln", "Madison", "Jefferson", "Adams",
        "Franklin", "Clinton", "Harrison", "Jackson", "Wilson", "Grant", "Sherman", "Roosevelt",
        "Kennedy", "Liberty", "Union", "Central", "Highland", "Woodland", "Meadow", "Garden",
        "Ridge", "Grove", "Brook", "Creek", "Mill", "Bridge", "Station", "Plaza",
        "Market", "Commerce"
    };

    // Street types
    private static final String[] STREET_TYPES = {
        "Street", "Avenue", "Boulevard", "Drive", "Road", "Lane", "Court", "Place",
        "Way", "Circle", "Terrace", "Parkway"
    };

    // City names
    private static final String[] CITIES = {
        "Springfield", "Franklin", "Clinton", "Madison", "Georgetown", "Bristol",
        "Salem", "Fairview", "Greenville", "Riverside", "Arlington", "Oakland",
        "Ashland", "Burlington", "Chester", "Dover", "Hudson", "Kingston",
        "Milton", "Newport"
    };

    // Email domains
    private static final String[] EMAIL_DOMAINS = {
        "company.com", "corp.com", "enterprise.com", "business.com", "organization.com"
    };

    // Phone area codes
    private static final String[] AREA_CODES = {
        "555", "556", "557", "558", "559", "560", "561", "562", "563", "564"
    };

    /**
     * Generates a random employee with unique identifiers based on the employee ID.
     * Uses the employee ID as a seed modifier to ensure reproducible but varied data.
     *
     * @param employeeId Unique employee identifier
     * @param departmentId Department to assign employee to (1-5)
     * @return Employee object with generated data
     */
    public static Employee generateRandomEmployee(int employeeId, int departmentId) {
        // Use employee ID to create deterministic but varied selections
        Random random = new Random(employeeId);

        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String fullName = firstName + " " + lastName;

        // Email: firstname.lastname.{id}@domain.com (ensures uniqueness)
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + employeeId
                + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];

        // Salary: $45,000 to $150,000
        double salaryAmount = 45000 + (random.nextDouble() * 105000);
        BigDecimal salary = BigDecimal.valueOf(salaryAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Hire date: Random date between 2018-01-01 and 2025-12-31
        LocalDate startDate = LocalDate.of(2018, 1, 1);
        long daysBetween = LocalDate.of(2025, 12, 31).toEpochDay() - startDate.toEpochDay();
        LocalDate hireDate = startDate.plusDays(Math.abs(random.nextLong() % daysBetween));

        // Phone: area-code + 4 random digits
        String phone = AREA_CODES[random.nextInt(AREA_CODES.length)] + "-"
                + String.format("%04d", random.nextInt(10000));

        // Address: street number + street name + type, city, state + zip
        int streetNumber = 100 + random.nextInt(9900);
        String streetName = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
        String streetType = STREET_TYPES[random.nextInt(STREET_TYPES.length)];
        String city = CITIES[random.nextInt(CITIES.length)];
        int zip = 10000 + random.nextInt(90000);
        String address = streetNumber + " " + streetName + " " + streetType + ", " + city + ", State " + zip;

        return new Employee(fullName, email, departmentId, salary, hireDate, phone, address);
    }

    /**
     * Simple Employee data holder class
     */
    public static class Employee {
        public final String name;
        public final String email;
        public final int departmentId;
        public final BigDecimal salary;
        public final LocalDate hireDate;
        public final String phone;
        public final String address;

        public Employee(String name, String email, int departmentId, BigDecimal salary,
                       LocalDate hireDate, String phone, String address) {
            this.name = name;
            this.email = email;
            this.departmentId = departmentId;
            this.salary = salary;
            this.hireDate = hireDate;
            this.phone = phone;
            this.address = address;
        }
    }
}
