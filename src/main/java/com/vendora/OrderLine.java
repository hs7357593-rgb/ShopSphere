package com.vendora;
import jakarta.persistence.Embeddable;
@Embeddable
public class OrderLine { public Long productId; public String name; public int quantity, price; }
