package com.example.ticketexpress;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<ShoppingItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(ShoppingItem item) {
        for (ShoppingItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setCartedCount(cartItem.getCartedCount() + 1);
                return;
            }
        }

        ShoppingItem newItem = new ShoppingItem(
                item.getName(),
                item.getInfo(),
                item.getPrice(),
                item.getRatedInfo(),
                item.getImageResource(),
                1
        );

        cartItems.add(newItem);
    }

    public List<ShoppingItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public float getTotalPrice() {
        float total = 0;
        for (ShoppingItem item : cartItems) {
            try {
                float price = Float.parseFloat(item.getPrice().replace("Ft", "").replace(",", ".").trim());
                total += price * item.getCartedCount();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return total;
    }
}
