package com.op.ludo.auth;

public enum Role {
  USER("USER"),
  ADMIN("ADMIN"),
  ANONYMOUS("ANONYMOUS");

  public static Role getRole(String role, Role defaultRole) {
    if (USER.toString().equalsIgnoreCase(role)) return USER;
    else if (ADMIN.toString().equalsIgnoreCase(role)) return ADMIN;
    else if (ANONYMOUS.toString().equalsIgnoreCase(role)) return ANONYMOUS;
    else return defaultRole;
  }

  String role;

  Role(String role) {
    this.role = role;
  }

  public String getAuthority() {
    return "ROLE_" + role;
  }

  @Override
  public String toString() {
    return role;
  }
}
