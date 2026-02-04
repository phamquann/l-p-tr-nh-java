package nhom8.minhquan.daos;

import lombok.*;
import nhom8.minhquan.entities.Book;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Cart {
    private List<Item> items = new ArrayList<>();
    
    public void addItem(Book book) {
        for (Item item : items) {
            if (item.getBookId().equals(book.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        items.add(new Item(book));
    }
    
    public void removeItem(Long bookId) {
        items.removeIf(item -> item.getBookId().equals(bookId));
    }
    
    public void updateQuantity(Long bookId, int quantity) {
        for (Item item : items) {
            if (item.getBookId().equals(bookId)) {
                if (quantity <= 0) {
                    removeItem(bookId);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }
    
    public void clear() {
        items.clear();
    }
    
    public int getTotalItems() {
        return items.stream().mapToInt(Item::getQuantity).sum();
    }
    
    public Double getTotalPrice() {
        return items.stream().mapToDouble(Item::getSubTotal).sum();
    }
}
