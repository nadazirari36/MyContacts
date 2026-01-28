package com.nada.mycontact.models;

public class ContactListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CONTACT = 1;

    private int type;
    private String header;
    private Contact contact;

    // Constructeur pour header
    public ContactListItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
    }

    // Constructeur pour contact
    public ContactListItem(Contact contact) {
        this.type = TYPE_CONTACT;
        this.contact = contact;
    }

    public int getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public Contact getContact() {
        return contact;
    }
}