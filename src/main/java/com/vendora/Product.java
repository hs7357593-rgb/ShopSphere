package com.vendora;
import jakarta.persistence.*;
@Entity
public class Product {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String name, category, sellerName, emoji, gradient;
  public int price, oldPrice, stock; public double rating; public Long sellerId; public boolean active=true;
  public static Product of(String n,String c,String s,int p,int o,double r,String e,String g,int st){
    Product x=new Product(); x.name=n;x.category=c;x.sellerName=s;x.price=p;x.oldPrice=o;x.rating=r;x.emoji=e;x.gradient=g;x.stock=st; return x; }
}
