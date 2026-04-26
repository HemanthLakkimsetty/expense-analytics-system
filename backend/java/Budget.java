package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Budget {

    private int        id;
    private String     category;
    private BigDecimal monthlyLimit;

    private BigDecimal spent     = BigDecimal.ZERO;
    private BigDecimal remaining = BigDecimal.ZERO;

    public Budget() {}

    public Budget(int id, String category, BigDecimal monthlyLimit) {
        this.id           = id;
        this.category     = category;
        this.monthlyLimit = monthlyLimit;
    }

    public int        getId()           { return id; }
    public String     getCategory()     { return category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public BigDecimal getSpent()        { return spent; }
    public BigDecimal getRemaining()    { return remaining; }

    public BigDecimal getPctUsed() {
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return spent.multiply(new BigDecimal("100"))
                    .divide(monthlyLimit, 2, RoundingMode.HALF_UP);
    }

    public boolean isOverBudget() {
        return spent != null && spent.compareTo(monthlyLimit) > 0;
    }

    public void setId(int id)                     { this.id           = id; }
    public void setCategory(String category)       { this.category     = category; }
    public void setMonthlyLimit(BigDecimal limit)  { this.monthlyLimit = limit; }
    public void setSpent(BigDecimal spent)          { this.spent        = spent != null ? spent : BigDecimal.ZERO; }
    public void setRemaining(BigDecimal remaining)  { this.remaining    = remaining != null ? remaining : BigDecimal.ZERO; }

    @Override
    public String toString() {
        return "Budget{id=" + id + ", category='" + category +
               "', limit=" + monthlyLimit + ", spent=" + spent + "}";
    }
}
