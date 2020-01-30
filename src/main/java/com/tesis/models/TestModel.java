package com.tesis.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "test_table")
@Getter
@Setter
public class TestModel extends AuditModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String test;
}
