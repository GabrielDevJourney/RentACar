package com.gabriel.rentacar.utils;

import com.gabriel.rentacar.exception.vehicleException.VehicleInvalidDataException;
import com.gabriel.rentacar.exception.vehicleException.VehicleInvalidPlateFormatException;
import com.gabriel.rentacar.exception.vehicleException.VehicleInvalidYearOfManufacture;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PlateValidation {
	private String normalizePlate(String plate) {
		if (plate == null || plate.trim().isEmpty()) {
			throw new VehicleInvalidDataException("plate", "Plate cannot be empty");
		}

		String normalizedPlate = plate.trim().toUpperCase();

		if (normalizedPlate.contains("-")) {
			return normalizePlateWithHyphens(normalizedPlate);
		} else if (normalizedPlate.length() == 6) {
			return normalizedNoHyphens(normalizedPlate);
		} else {
			throw new VehicleInvalidPlateFormatException(normalizedPlate,
					"Please insert a valid format XX-XX-XX or XXXXXX");
		}
	}

	private String normalizedNoHyphens(String plate) {
		String[] platePartsNoHyphen = new String[3];

		//no characters allowed
		for (int i = 0; i < 6; i++) {
			char character = plate.charAt(i);
			if (!(character >= 'A' && character <= 'Z') && !(character >= '0' && character <= '9')) {
				throw new VehicleInvalidPlateFormatException(plate,
						"Plate can only contain letters A-Z and numbers 0-9");
			}
		}

		// After validation, extract the segments
		platePartsNoHyphen[0] = plate.substring(0, 2);
		platePartsNoHyphen[1] = plate.substring(2, 4);
		platePartsNoHyphen[2] = plate.substring(4, 6);

		return platePartsNoHyphen[0] + "-" + platePartsNoHyphen[1] + "-" + platePartsNoHyphen[2];
	}

	private String normalizePlateWithHyphens(String plate) {
		String[] platePartsWithoutHyphens = new String[3];

		String[] platePartsWithHyphen = plate.split("-");

		if (platePartsWithHyphen.length != 3) {
			throw new VehicleInvalidPlateFormatException(plate,
					"Plate must have exactly 3 segments separated by hyphens");
		}

		for (int i = 0; i < 3; i++) {
			String part = platePartsWithHyphen[i].trim();

			if (part.length() != 2) {
				throw new VehicleInvalidPlateFormatException(plate,
						"Each plate segment must be exactly 2 characters");
			}

			// no carachters allowed
			for (char character : part.toCharArray()) {
				if (!(character >= 'A' && character <= 'Z') && !(character >= '0' && character <= '9')) {
					throw new VehicleInvalidPlateFormatException(plate,
							"Plate segments can only contain letters A-Z and numbers 0-9");
				}
			}

			platePartsWithoutHyphens[i] = part;
		}

		return platePartsWithoutHyphens[0] + "-" + platePartsWithoutHyphens[1] + "-" + platePartsWithoutHyphens[2];
	}
	public  String validatePlateFormat(String plate, int yearManufacture) {
		String normalizedPlate = normalizePlate(plate);

		if (yearManufacture > 2020) {
			// AA-00-AA format
			if (!normalizedPlate.matches("^[A-Z]{2}-[0-9]{2}-[A-Z]{2}$")) {
				throw new VehicleInvalidPlateFormatException(
						normalizedPlate,
						"For vehicles after 2020, plate must be in AA-00-AA format"
				);
			}
		} else if (yearManufacture >= 2005) {
			// 00-AA-00 format
			if (!normalizedPlate.matches("^[0-9]{2}-[A-Z]{2}-[0-9]{2}$")) {
				throw new VehicleInvalidPlateFormatException(
						normalizedPlate,
						"For vehicles after 2005, plate must be in 00-AA-00 format"
				);
			}
		} else {
			throw new VehicleInvalidYearOfManufacture(
					yearManufacture,
					(LocalDate.now().getYear() - 20),
					LocalDate.now().getYear()
			);
		}

		return normalizedPlate;
	}
}
