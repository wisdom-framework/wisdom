package controllers;

/**
 * An object received using a form.
 */
public class FormObject {

    public String name;

    public int primInt;
    public long primLong;
    public float primFloat;
    public double primDouble;

    public Integer objInt;
    public Long objLong;
    public Float objFloat;
    public Double objDouble;


    private String email;

    private boolean primBoolean;
    private byte primByte;
    private short primShort;
    private char primChar;

    private Boolean objBoolean;
    private Byte objByte;
    private Short objShort;
    private Character objChar;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPrimBoolean() {
        return primBoolean;
    }

    public void setPrimBoolean(boolean primBoolean) {
        this.primBoolean = primBoolean;
    }

    public byte getPrimByte() {
        return primByte;
    }

    public void setPrimByte(byte primByte) {
        this.primByte = primByte;
    }

    public short getPrimShort() {
        return primShort;
    }

    public void setPrimShort(short primShort) {
        this.primShort = primShort;
    }

    public char getPrimChar() {
        return primChar;
    }

    public void setPrimChar(char primChar) {
        this.primChar = primChar;
    }

    public Boolean getObjBoolean() {
        return objBoolean;
    }

    public void setObjBoolean(Boolean objBoolean) {
        this.objBoolean = objBoolean;
    }

    public Byte getObjByte() {
        return objByte;
    }

    public void setObjByte(Byte objByte) {
        this.objByte = objByte;
    }

    public Short getObjShort() {
        return objShort;
    }

    public void setObjShort(Short objShort) {
        this.objShort = objShort;
    }

    public Character getObjChar() {
        return objChar;
    }

    public void setObjChar(Character objChar) {
        this.objChar = objChar;
    }
}
