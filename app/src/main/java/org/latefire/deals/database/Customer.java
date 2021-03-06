package org.latefire.deals.database;

/**
 * Created by dw on 19/03/17.
 */

public class Customer extends AbsUser {

  private String firstName;
  private String lastName;

  public Customer() {
  }

  public Customer(String email, String phone, String firstName, String lastName) {
    super(email, phone);
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getName() {
    return firstName + " " + lastName;
  }
}
