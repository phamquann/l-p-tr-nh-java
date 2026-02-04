package nhom8.minhquan.daos;

import lombok.*;
import nhom8.minhquan.entities.Book;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long bookId;
    private String title;
    private String author;
    private Double price;
    private int quantity;
    
    public Item(Book book) {
        this.bookId = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.price = book.getPrice();
        this.quantity = 1;
    }
    
    public Double getSubTotal() {
        return price * quantity;
    }
}
