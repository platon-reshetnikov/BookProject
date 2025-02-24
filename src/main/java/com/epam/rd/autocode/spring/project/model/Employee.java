package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Employee extends User{
    private String phone;
    private LocalDate birthDate;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
