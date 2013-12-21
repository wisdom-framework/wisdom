package controllers.json;

public class Car {

    private final String brand;

    private final String model;

    private final int power;

    private final String color;

    public Car(String brand, String model, int power, String color) {
        this.brand = brand;
        this.model = model;
        this.power = power;
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getPower() {
        return power;
    }

    public String getColor() {
        return color;
    }
}
