package com.mylibrary.mapper;

import com.mylibrary.dto.reader.ReaderDTO;
import com.mylibrary.entity.Reader;

public class ReaderMapper {
    public static ReaderDTO toReaderDTO(Reader reader) {
        return new ReaderDTO(
                reader.getId(),
                reader.getFirstName(),
                reader.getLastName(),
                reader.getEmail(),
                reader.getPhone(),
                reader.getRegistrationDate());
    }
}
