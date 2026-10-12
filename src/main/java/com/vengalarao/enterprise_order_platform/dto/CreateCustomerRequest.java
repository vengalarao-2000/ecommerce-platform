package com.vengalarao.enterprise_order_platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCustomerRequest {

    @NotBlank 
    @Size (max = 100)
    private String firstName;
    @NotBlank 
    @Size (max = 100)
    private String lastName;
    @NotBlank 
    @Email 
    @Size (max = 255)
    private String email;
    @Size (max = 20 )
    private String phone;

    public CreateCustomerRequest() {
    }

    //Don't include id, active as those are controlled by server.

    public CreateCustomerRequest(@NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName, @NotBlank @Email @Size(max = 255) String email,
            @Size(max = 20) String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
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
