package com.survey.application.services;


import com.survey.application.dtos.UpdatedSensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.domain.models.SensorMac;
import com.survey.domain.repository.SensorMacRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class SensorMacServiceImpl implements SensorMacService{
    private final SensorMacRepository sensorMacRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    @Autowired
    public SensorMacServiceImpl(SensorMacRepository sensorMacRepository, ModelMapper modelMapper, EntityManager entityManager) {
        this.sensorMacRepository = sensorMacRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<SensorMacDtoOut> saveSensorMacList(List<SensorMacDtoIn> dtoList) {
        List<SensorMac> sensorMacEntityList = dtoList.stream()
                .map(dto -> {
                    SensorMac sensorMac = modelMapper.map(dto, SensorMac.class);
                    sensorMac.setSensorMac(sensorMac.getSensorMac().toUpperCase());
                    return sensorMac;
                })
                .toList();

        List<SensorMac> savedEntities = sensorMacRepository.saveAllAndFlush(sensorMacEntityList);
        savedEntities.forEach(entityManager::refresh);

        return savedEntities.stream()
                .map(entity -> modelMapper.map(entity, SensorMacDtoOut.class))
                .toList();
    }

    @Override
    public void deleteSensorMac(String sensorId) {
        Optional<SensorMac> optionalSensorMac = sensorMacRepository.findBySensorId(sensorId);
        if (optionalSensorMac.isPresent()){
            sensorMacRepository.delete(optionalSensorMac.get());
        } else {
            throw new NoSuchElementException("Sensor with sensorId " + sensorId + " not found.");
        }
    }

    @Override
    public SensorMacDtoOut updateSensorMacBySensorId(String sensorId, UpdatedSensorMacDtoIn updatedSensorMacDtoIn) {
        SensorMac existingSensorMac = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));

        existingSensorMac.setSensorMac(updatedSensorMacDtoIn.getSensorMac().toUpperCase());

        SensorMac updatedEntity = sensorMacRepository.save(existingSensorMac);

        return modelMapper.map(updatedEntity, SensorMacDtoOut.class);
    }

    @Override
    public List<SensorMacDtoOut> getFullSensorMacList() {
        List<SensorMac> sensorMacList = sensorMacRepository.findAllOrderBySensorId();

        return sensorMacList.stream()
                .map(entity -> modelMapper.map(entity, SensorMacDtoOut.class))
                .toList();
    }

    @Override
    public SensorMacDtoOut getSensorMacBySensorId(String sensorId) {
        SensorMac sensorMac = sensorMacRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new NoSuchElementException("Sensor with sensorId " + sensorId + " not found."));

        return modelMapper.map(sensorMac, SensorMacDtoOut.class);
    }
}
