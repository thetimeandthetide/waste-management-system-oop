package model;

// Inheritance: RecyclableWaste extends the abstract Waste class.
public class RecyclableWaste extends Waste {
    public RecyclableWaste() {
    }

    public RecyclableWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Recyclable";
    }
}
