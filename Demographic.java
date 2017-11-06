/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 * This class contains the value of a particular student. Demographic in this case is a person.
 * @author Terminal 4
 */
public class Demographic {

    private String macAddress;
    private String name;
    private String password;
    private String email;
    private char gender;

    /**
     * Constructor
     * @param macAddress Demographic email
     * @param name Demographic name
     * @param password Demographic password
     * @param email Demographic email
     * @param gender Demographic gender
     */
    public Demographic(String macAddress, String name, String password, String email, char gender) {
        this.macAddress = macAddress;
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
    }
    
    /**
     * Returns macAddress of the Demographic
     * @return String mac address
     */
    public String getMacAddress() {
        return macAddress;
    }
    
    /**
     * Returns the name of the Demographic
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the password of the Demographic
     * @return String password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Returns the email of the Demographic
     * @return String email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the gender of the Demographic
     * @return char gender (M or F)
     */
    public char getGender() {
        return gender;
    }
}
