package com.xb.geo.springbatch.entity;

import lombok.*;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "address_repository_s")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressStageRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "shipment_id")
	private String shipmentId;
	
	@Column(name = "address_line")
	private String addressLine;
	
	@Column(name = "city_code")
	private String cityCode;
	
	@Column(name = "pincode")
	private String pincode;
	
	@Column(name = "state_code")
	private String stateCode;
	
	@Column(name = "country_code")
	private String countryCode;
	
	@Column(name = "region_code")
	private String regionCode;
	
	@Column(name = "layer_id")
	private Integer layerId;
	
	@Column(name = "latitude")
	private String latitude;
	
	@Column(name = "longitude")
	private String longitude;
	
	@Column(name = "geocode")
	private Geometry geocode; // Change the type to appropriate spatial type if needed
	
	@Column(name = "geo_accuracy_level")
	private Double geoAccuracyLevel;
	
	@Column(name = "is_inside_geofence")
	private Boolean isInsideGeofence;
	
	@Column(name = "is_manual_scan")
	private Boolean isManualScan;
	
	@Column(name = "is_offline_submit")
	private Boolean isOfflineSubmit;
	
	@Column(name = "is_geo_enabled")
	private Boolean isGeoEnabled;
	
	@Column(name = "created_date")
	private Timestamp createdDate;
	
	@Column(name = "purge_date")
	private Timestamp purgeDate;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "region")
	private String region;
	
	
	// Getters and setters
}
