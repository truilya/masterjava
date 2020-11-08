package ru.javaops.masterjava.service.mail.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailEntity {
    @Column("time_send")
    private LocalDateTime timestamp;
    private @NonNull String result;
    @Column("error_cause")
    private String cause;
}
