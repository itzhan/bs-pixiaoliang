package com.parking.smart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出入场记录（无 updatedAt、无逻辑删除）
 */
@Data
@TableName("entry_exit_log")
public class EntryExitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNumber;

    /**
     * 记录类型：ENTRY-入场，EXIT-出场
     */
    private String logType;

    private String gateName;

    private String capturedImage;

    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
