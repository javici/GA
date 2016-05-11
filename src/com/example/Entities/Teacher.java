package com.example.Entities;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.rowset.serial.SerialArray;

@Entity
@Table(name = "docent")
public class Teacher implements Serializable {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String nom;
	private String cognoms;
	private String email;
	
	public Teacher() {
		
	}
	

	public Teacher(int id, String nom,String cognoms, String email) {
		super();
		this.id = id;
		this.nom = nom;
		this.cognoms = cognoms;
		this.email = email;
	}
	
	public Teacher(String nom,String cognoms, String email) {
		super();
		this.cognoms = cognoms;
		this.nom = nom;
		this.email = email;
	}
	
	
	
	public String getCognoms() {
		return cognoms;
	}


	public void setCognoms(String cognoms) {
		cognoms = cognoms;
	}


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}


	@Override
	public String toString() {
		return "Docent [id=" + id + ", nom=" + nom + ", Cognoms=" + cognoms + ", email=" + email + "]";
	}
	

}
