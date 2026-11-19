package com.texsetu.dto;

import com.texsetu.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role must be specified")
    private Role role; // BUYER or JOB_WORKER

    // Business Profile Fields
    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business type is required")
    private String businessType;

    private String gstNumber;
    private String panNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city = "Surat";

    @NotBlank(message = "State is required")
    private String state = "Gujarat";

    @NotBlank(message = "Pincode is required")
    private String pincode;

    // Job-Worker Specific
    private String machineryDetails;
    private Double indicativeCapacity = 1000.0;
    private Integer turnaroundDays = 3;

    public UserRegistrationDto() {}

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getMachineryDetails() { return machineryDetails; }
    public void setMachineryDetails(String machineryDetails) { this.machineryDetails = machineryDetails; }

    public Double getIndicativeCapacity() { return indicativeCapacity; }
    public void setIndicativeCapacity(Double indicativeCapacity) { this.indicativeCapacity = indicativeCapacity; }

    public Integer getTurnaroundDays() { return turnaroundDays; }
    public void setTurnaroundDays(Integer turnaroundDays) { this.turnaroundDays = turnaroundDays; }
}
