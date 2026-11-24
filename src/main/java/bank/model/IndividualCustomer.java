package bank.model;

public class IndividualCustomer extends Customer {
    private final String firstName;
    private final String surname;

    public IndividualCustomer(String firstName, String surname, String address,
                              String phone, String username, String passwordHash) {
        super(address, phone, username, passwordHash);
        if (firstName == null || firstName.isBlank() || surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("First name and surname are required");
        }
        this.firstName = firstName.trim();
        this.surname = surname.trim();
    }

    public IndividualCustomer(String firstName, String surname, String address) {
        this(firstName, surname, address, "", null, null);
    }

    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }

    @Override public String getDisplayName() { return firstName + " " + surname; }
    @Override public String getCustomerType() { return "INDIVIDUAL"; }
}
