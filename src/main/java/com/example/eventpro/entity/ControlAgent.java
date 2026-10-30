package com.example.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "control_agents")
@Getter
@Setter
@NoArgsConstructor
public class ControlAgent extends User {
}