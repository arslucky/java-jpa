package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contract")
public class Contract {
    @Id
    private Long id;
    private String number;
    private LocalDateTime createDate;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
