package bank.model;

public class CompanyCustomer extends Customer {
    private final String companyName;

    public CompanyCustomer(String companyName, String address, String phone,
                           String username, String passwordHash) {
        super(address, phone, username, passwordHash);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        this.companyName = companyName.trim();
    }

    public CompanyCustomer(String companyName, String address) {
        this(companyName, address, "", null, null);
    }

    public String getCompanyName() { return companyName; }

    @Override public String getDisplayName() { return companyName; }
    @Override public String getCustomerType() { return "COMPANY"; }
}
