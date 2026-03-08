package com.parking.smart.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Integer page;
    private Integer size;

    public static <T> PageResult<T> from(IPage<T> iPage) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(iPage.getRecords());
        result.setTotal(iPage.getTotal());
        result.setPage((int) iPage.getCurrent());
        result.setSize((int) iPage.getSize());
        return result;
    }
}
