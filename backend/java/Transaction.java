package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {

    private int        id;
    private LocalDate  date;
    private String     type;        
    private String     category;
    private BigDecimal amount;
    private String     description;

    public Transaction() {}

    public Transaction(int id, LocalDate date, String type,
                       String category, BigDecimal amount, String description) {
        this.id          = id;
        this.date        = date;
        this.type        = type;
        this.category    = category;
        this.amount      = amount;
        this.description = description;
    }

    public int        getId()          { return id; }
    public LocalDate  getDate()        { return date; }
    public String     getType()        { return type; }
    public String     getCategory()    { return category; }
    public BigDecimal getAmount()      { return amount; }
    public String     getDescription() { return description; }

    public void setId(int id)                   { this.id = id; }
    public void setDate(LocalDate date)          { this.date = date; }
    public void setType(String type)             { this.type = type; }
    public void setCategory(String category)     { this.category = category; }
    public void setAmount(BigDecimal amount)      { this.amount = amount; }
    public void setDescription(String description){ this.description = description; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", date=" + date + ", type='" + type +
               "', category='" + category + "', amount=" + amount + "}";
    }
}
