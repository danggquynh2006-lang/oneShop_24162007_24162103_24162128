package com.oneshop.shipper;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Kết quả thống kê của một shipper (dùng cho trang /shipper/statistics). */
@Getter
@AllArgsConstructor
public class ShipperStatistics {

    private final int year;
    private final int month;

    private final long deliveredInMonth;
    private final BigDecimal codInMonth;
    private final long deliveredInYear;
    private final BigDecimal codInYear;

    private final long waitingPickup;
    private final long inProgress;
    private final long failed;

    /** Từng ngày trong tháng đã chọn. */
    private final List<Row> days;
    /** Từng tháng trong năm đã chọn. */
    private final List<Row> months;

    @Getter
    @AllArgsConstructor
    public static class Row {
        private final String label;
        private final long delivered;
        private final BigDecimal cod;
    }
}