package com.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizers")
@Getter
@Setter
@NoArgsConstructor
public class Organizer extends User {

    private String organisationName;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "organizer")
    private List<Promotion> promotions = new ArrayList<>();
}