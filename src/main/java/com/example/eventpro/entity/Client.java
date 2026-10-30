package com.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client extends User {

    private String phone;

    @OneToMany(mappedBy = "client")
    private List<Reservation> reservations = new ArrayList<>();
}