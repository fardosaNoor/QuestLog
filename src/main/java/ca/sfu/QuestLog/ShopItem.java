package ca.sfu.QuestLog;

import jakarta.persistence.*;

@Entity
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String icon;

    protected ShopItem() {}

    public ShopItem(String name, String category, int price, String icon) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.icon = icon;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public String getIcon() {
        return icon;
    }
}