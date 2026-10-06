package com.retailsystem.config;

import com.retailsystem.entity.Branch;
import com.retailsystem.entity.CashierEmployee;
import com.retailsystem.entity.Category;
import com.retailsystem.entity.Customer;
import com.retailsystem.entity.Employee;
import com.retailsystem.entity.ExpiryDiscountRule;
import com.retailsystem.entity.InventoryBatch;
import com.retailsystem.entity.InventoryStaffEmployee;
import com.retailsystem.entity.ManagerEmployee;
import com.retailsystem.entity.PayrollRecord;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Supplier;
import com.retailsystem.entity.User;
import com.retailsystem.enums.BranchStatus;
import com.retailsystem.enums.LoyaltyTier;
import com.retailsystem.enums.Role;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.CategoryRepository;
import com.retailsystem.repository.CustomerRepository;
import com.retailsystem.repository.EmployeeRepository;
import com.retailsystem.repository.ExpiryDiscountRuleRepository;
import com.retailsystem.repository.InventoryBatchRepository;
import com.retailsystem.repository.PayrollRecordRepository;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SupplierRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds demo data so the app isn't empty on first login. Each piece is
 * checked independently (by email / branch code / category name / etc.)
 * rather than gating the whole method on "are there zero users" — that
 * earlier version meant that anyone who had already run an earlier phase
 * (and so already had at least the Super Admin user) would never get later
 * demo data seeded in, since the users table was no longer empty. This
 * version is safe to run repeatedly against a database that already has
 * some of this data — it only inserts what's actually missing.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    @Autowired
    private ExpiryDiscountRuleRepository expiryDiscountRuleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedSuperAdmin();

        Branch downtown = seedBranch("Downtown Branch", "DT01", "12 Market Street", "Chennai", "044-2200-1010");
        Branch riverside = seedBranch("Riverside Branch", "RS02", "45 River Road", "Chennai", "044-2200-2020");

        seedUser("Asha Menon", "manager.downtown@retailsystem.com", "Manager@123", Role.BRANCH_MANAGER, downtown);
        seedUser("Rahul Nair", "manager.riverside@retailsystem.com", "Manager@123", Role.BRANCH_MANAGER, riverside);
        seedUser("Priya Suresh", "cashier.downtown@retailsystem.com", "Cashier@123", Role.CASHIER, downtown);

        seedCategory("Groceries", "Staple foods and pantry items");
        seedCategory("Dairy", "Milk, curd, cheese, and other dairy products");
        seedCategory("Beverages", "Soft drinks, juices, and packaged beverages");
        seedCategory("Household", "Cleaning and general household supplies");

        seedProduct("Basmati Rice 5kg", "GRO-RICE-5KG", "Groceries",
                new BigDecimal("650.00"), 40, 15, LocalDate.now().plusMonths(8), downtown);
        seedProduct("Sunflower Oil 1L", "GRO-OIL-1L", "Groceries",
                new BigDecimal("180.00"), 8, 10, LocalDate.now().plusDays(10), downtown);
        seedProduct("Toned Milk 1L", "DRY-MILK-1L", "Dairy",
                new BigDecimal("60.00"), 25, 20, LocalDate.now().plusDays(4), downtown);
        seedProduct("Whole Wheat Atta 10kg", "GRO-ATTA-10KG", "Groceries",
                new BigDecimal("420.00"), 18, 10, LocalDate.now().plusMonths(6), riverside);
        seedProduct("Toned Milk 1L", "DRY-MILK-1L", "Dairy",
                new BigDecimal("60.00"), 5, 20, LocalDate.now().plusDays(3), riverside);

        seedSupplier("Sundar Wholesale Traders", "Sundar Rajan", "044-4455-6677",
                "sundar@wholesale.example.com", "22 Wholesale Market Road, Chennai", "33AAAAA0000A1Z5");
        seedSupplier("Amman Dairy Distributors", "Kavitha Amman", "044-9988-7766",
                "kavitha@ammandairy.example.com", "8 Cool Storage Lane, Chennai", "33BBBBB1111B2Z6");

        // Points/tier pairs below mirror CustomerService's thresholds (500/2000/5000) so the
        // demo data already shows the tier badges differentiating, without needing Billing to exist.
        seedCustomer("Divya Prakash", "9840011122", "divya.prakash@example.com", "14 Anna Nagar, Chennai", 0, LoyaltyTier.BRONZE);
        seedCustomer("Karthik Subramaniam", "9840033344", "karthik.s@example.com", "27 T Nagar, Chennai", 650, LoyaltyTier.SILVER);
        seedCustomer("Meena Ramaswamy", "9840055566", "meena.r@example.com", "9 Adyar, Chennai", 2400, LoyaltyTier.GOLD);

        Employee dtManager = seedManagerEmployee("EMP-DT-MGR-01", "Asha Menon", "9940011001", "manager.downtown@retailsystem.com",
                downtown, LocalDate.now().minusYears(2), new BigDecimal("55000.00"), new BigDecimal("5000.00"), new BigDecimal("2000.00"));
        Employee dtCashier = seedCashierEmployee("EMP-DT-CSH-01", "Priya Suresh", "9940011002", "cashier.downtown@retailsystem.com",
                downtown, LocalDate.now().minusMonths(8), new BigDecimal("22000.00"), new BigDecimal("2000.00"), new BigDecimal("800.00"));
        seedInventoryStaffEmployee("EMP-DT-INV-01", "Elango Murugan", "9940011003", "elango.m@example.com",
                downtown, LocalDate.now().minusMonths(5), new BigDecimal("20000.00"), new BigDecimal("1500.00"), new BigDecimal("700.00"));
        seedManagerEmployee("EMP-RS-MGR-01", "Rahul Nair", "9940022001", "manager.riverside@retailsystem.com",
                riverside, LocalDate.now().minusYears(1), new BigDecimal("55000.00"), new BigDecimal("5000.00"), new BigDecimal("2000.00"));

        // One pre-generated payslip each, for last month, so the payroll history table isn't
        // empty on first look — everything else about payroll generation still has to be
        // triggered by the user through the UI.
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        seedPayroll(dtManager, lastMonth.getMonthValue(), lastMonth.getYear());
        seedPayroll(dtCashier, lastMonth.getMonthValue(), lastMonth.getYear());

        // Module 10's example ladder, seeded as a starting point — fully editable/deletable by
        // the Super Admin from the Expiry Discounts screen, not hard-coded logic.
        seedExpiryDiscountRule("Approaching expiry", 31, 60, new BigDecimal("5.00"));
        seedExpiryDiscountRule("Near expiry", 15, 30, new BigDecimal("10.00"));
        seedExpiryDiscountRule("Clearance", 1, 14, new BigDecimal("20.00"));

        backfillMissingBarcodes();
    }

    private void backfillMissingBarcodes() {
        for (Product product : productRepository.findAll()) {
            if (product.getBarcode() == null || product.getBarcode().isBlank()) {
                product.setBarcode(product.getSku());
                productRepository.save(product);
            }
        }
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByEmail("admin@retailsystem.com")) {
            return;
        }
        User admin = new User();
        admin.setFullName("Super Admin");
        admin.setEmail("admin@retailsystem.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.SUPER_ADMIN);
        admin.setActive(true);
        admin.setBranch(null);
        userRepository.save(admin);
        System.out.println("Seeded: Super Admin (admin@retailsystem.com)");
    }

    private Branch seedBranch(String name, String code, String address, String city, String phone) {
        return branchRepository.findByBranchCode(code).orElseGet(() -> {
            Branch branch = branchRepository.save(new Branch(name, code, address, city, phone, BranchStatus.ACTIVE));
            System.out.println("Seeded: Branch " + code);
            return branch;
        });
    }

    private void seedUser(String fullName, String email, String rawPassword, Role role, Branch branch) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User(fullName, email, passwordEncoder.encode(rawPassword), role, true, branch);
        userRepository.save(user);
        System.out.println("Seeded: " + role + " (" + email + ")");
    }

    private void seedProduct(String name, String sku, String categoryName, BigDecimal price,
                              int quantity, int reorderLevel, LocalDate expiryDate, Branch branch) {
        if (productRepository.existsByBranchIdAndSkuIgnoreCase(branch.getId(), sku)) {
            return;
        }
        Category category = categoryRepository.findByNameIgnoreCase(categoryName).orElse(null);
        Product product = new Product(name, sku, category, price, quantity, reorderLevel, expiryDate, branch);
        product.setBarcode(sku);
        product = productRepository.save(product);
        // Mirrors InventoryBatchService#createOpeningBatch — seeded products go through the
        // repository directly (like every other entity in this seeder), not ProductService, so
        // they need their own opening batch here or FIFO deduction would find nothing to sell
        // from at checkout despite Product.quantity showing stock.
        if (quantity > 0) {
            inventoryBatchRepository.save(new InventoryBatch(
                    "OPENING-" + sku, product, branch, quantity, null, expiryDate, null, LocalDate.now()));
        }
        System.out.println("Seeded: Product " + sku + " @ " + branch.getBranchCode());
    }

    private Category seedCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = categoryRepository.save(new Category(name, description));
            System.out.println("Seeded: Category " + name);
            return category;
        });
    }

    private void seedSupplier(String name, String contactPerson, String phone, String email, String address, String gstNumber) {
        if (!supplierRepository.findByNameContainingIgnoreCase(name).isEmpty()) {
            return;
        }
        supplierRepository.save(new Supplier(name, contactPerson, phone, email, address, gstNumber));
        System.out.println("Seeded: Supplier " + name);
    }

    private void seedCustomer(String fullName, String phone, String email, String address, int loyaltyPoints, LoyaltyTier tier) {
        if (customerRepository.existsByPhone(phone)) {
            return;
        }
        Customer customer = new Customer(fullName, phone, email, address);
        customer.setLoyaltyPoints(loyaltyPoints);
        customer.setLoyaltyTier(tier);
        customerRepository.save(customer);
        System.out.println("Seeded: Customer " + fullName);
    }

    private Employee seedManagerEmployee(String code, String fullName, String phone, String email, Branch branch,
                                          LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            return null;
        }
        Employee employee = employeeRepository.save(new ManagerEmployee(
                code, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions));
        System.out.println("Seeded: Employee (Manager) " + code);
        return employee;
    }

    private Employee seedCashierEmployee(String code, String fullName, String phone, String email, Branch branch,
                                          LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            return null;
        }
        Employee employee = employeeRepository.save(new CashierEmployee(
                code, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions));
        System.out.println("Seeded: Employee (Cashier) " + code);
        return employee;
    }

    private Employee seedInventoryStaffEmployee(String code, String fullName, String phone, String email, Branch branch,
                                                 LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            return null;
        }
        Employee employee = employeeRepository.save(new InventoryStaffEmployee(
                code, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions));
        System.out.println("Seeded: Employee (Inventory Staff) " + code);
        return employee;
    }

    private void seedPayroll(Employee employee, int month, int year) {
        if (employee == null || payrollRecordRepository.existsByEmployee_IdAndPeriodMonthAndPeriodYear(employee.getId(), month, year)) {
            return;
        }
        payrollRecordRepository.save(new PayrollRecord(
                employee, month, year,
                employee.getBasicSalary(), employee.getAllowances(),
                employee.calculateMonthlyBonus(), employee.getDeductions(), employee.calculateNetPay()
        ));
        System.out.println("Seeded: Payroll for " + employee.getFullName() + " (" + month + "/" + year + ")");
    }

    private void seedExpiryDiscountRule(String label, int minDays, int maxDays, BigDecimal discountPercent) {
        boolean alreadyExists = expiryDiscountRuleRepository.findAllByOrderByMinDaysBeforeExpiryAsc().stream()
                .anyMatch(r -> r.getMinDaysBeforeExpiry() == minDays && r.getMaxDaysBeforeExpiry() == maxDays);
        if (alreadyExists) {
            return;
        }
        expiryDiscountRuleRepository.save(new ExpiryDiscountRule(label, minDays, maxDays, discountPercent));
        System.out.println("Seeded: Expiry discount rule '" + label + "' (" + minDays + "-" + maxDays + " days, " + discountPercent + "%)");
    }
}
