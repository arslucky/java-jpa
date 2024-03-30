package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class Customer {
    @Id
    private Long id;
    private String name;
    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private List<Contract> contracts;
}
