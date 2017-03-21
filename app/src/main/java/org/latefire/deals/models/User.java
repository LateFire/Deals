package org.latefire.deals.models;

/**
 * Created by dw on 19/03/17.
 */

public abstract class User extends FirebaseModel {

  private String email;
  private String phone;

  public User() {
  }

  public User(String email, String phone) {
    this.email = email;
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
