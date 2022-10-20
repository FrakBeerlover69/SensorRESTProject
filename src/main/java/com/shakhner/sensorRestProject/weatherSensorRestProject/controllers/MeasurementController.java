package com.shakhner.sensorRestProject.weatherSensorRestProject.controllers;

import com.shakhner.sensorRestProject.weatherSensorRestProject.dto.MeasurementDTO;
import com.shakhner.sensorRestProject.weatherSensorRestProject.dto.response.MeasurementResponseByLocation;
import com.shakhner.sensorRestProject.weatherSensorRestProject.dto.response.MeasurementResponseBySensor;
import com.shakhner.sensorRestProject.weatherSensorRestProject.services.MeasurementService;
import com.shakhner.sensorRestProject.weatherSensorRestProject.services.SensorService;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.Converter;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.ExceptionInfoCreator;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.exceptions.MeasurementExceprions.MeasurementNotCreatedException;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.exceptions.MeasurementExceprions.MeasurementNotFoundException;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.exceptions.ExceptionsResponse;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.exceptions.sensorExceptions.SensorNotFoundException;
import com.shakhner.sensorRestProject.weatherSensorRestProject.util.validators.MeasurementValidator;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/measurement")
public class MeasurementController {
    private final ModelMapper modelMapper;
    private final MeasurementService measurementService;
    private final SensorService sensorService;
    private final Converter converter;
    private final MeasurementValidator measurementValidator;

    public MeasurementController(ModelMapper modelMapper, MeasurementService measurementService, SensorService sensorService, Converter converter, MeasurementValidator measurementValidator) {
        this.modelMapper = modelMapper;
        this.measurementService = measurementService;
        this.sensorService = sensorService;
        this.converter = converter;
        this.measurementValidator = measurementValidator;
    }

    @PostMapping
    @RequestMapping("/add")
    public ResponseEntity<HttpStatus> addMeasurement(@RequestBody @Valid MeasurementDTO measurementDTO,
                                                     BindingResult bindingResult) throws ParseException {

        measurementValidator.validate(measurementDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            String errors = ExceptionInfoCreator.getInfo(bindingResult);

            throw new MeasurementNotCreatedException(errors);
        }

        measurementService.saveMeasurement(converter.convertToMeasurement(measurementDTO));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/getDataForSensorsName")
    public List<MeasurementResponseBySensor> getMeasurementsBySensor(@RequestParam("sensorId") int sensorId) {

        return sensorService.getMeasurementsList(sensorId).stream()
                .map(converter::convertToMeasurementResponseBySensor).collect(Collectors.toList());

    }


    @GetMapping("/getDataForLocation")
    public List<MeasurementResponseByLocation> getMeasurementsByLocation(@RequestParam("location") String location) {

        return measurementService.getByLocationOfMeasurement(location).stream().map(converter::convertToMeasurementResponseByLocation).collect(Collectors.toList());

    }

    @GetMapping("/getDataForLocationAndDate")
    public List<MeasurementResponseByLocation> getMeasurementsByLocationAndDate(@RequestParam("location") String location,
                                                                                @RequestParam(value = "from") String from,
                                                                                @RequestParam("to") String to) {
        return measurementService.getDateByLocationBetween(location, from, to).stream()
                .map(converter::convertToMeasurementResponseByLocation).collect(Collectors.toList());
    }

    @ExceptionHandler(MeasurementNotFoundException.class)
    private ResponseEntity<ExceptionsResponse> measurementNotCreatedExceptionHandler(MeasurementNotFoundException e) {
        ExceptionsResponse response = new ExceptionsResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SensorNotFoundException.class)
    private ResponseEntity<ExceptionsResponse> sensorNotFoundExceptionHandler(SensorNotFoundException e) {
        ExceptionsResponse response = new ExceptionsResponse("Sensor not found");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MeasurementNotCreatedException.class)
    private ResponseEntity<ExceptionsResponse> measurementNotCreatedExceptionHandler(MeasurementNotCreatedException e) {
        ExceptionsResponse response = new ExceptionsResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
