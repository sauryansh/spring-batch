package com.xb.geo.springbatch.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;

@Data
@Setter
@Getter
@ToString
public class CsvData {
    @Column(name = "message_id")
    private String messageId;
    @Column(name = "address_line")
    private String addressLine;
    @Column(name = "pincode")
    private String pincode;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "latitude")
    private String latitude;
    @Column(name = "longitude")
    private String longitude;
}