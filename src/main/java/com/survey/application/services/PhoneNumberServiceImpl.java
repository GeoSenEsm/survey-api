package com.survey.application.services;

import com.survey.application.dtos.PhoneNumberDtoIn;
import com.survey.application.dtos.PhoneNumberDtoOut;
import com.survey.domain.models.PhoneNumber;
import com.survey.domain.repository.PhoneNumberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PhoneNumberServiceImpl implements PhoneNumberService{
    private final PhoneNumberRepository phoneNumberRepository;
    private final ModelMapper modelMapper;
    @Autowired
    public PhoneNumberServiceImpl(PhoneNumberRepository phoneNumberRepository, ModelMapper modelMapper) {
        this.phoneNumberRepository = phoneNumberRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PhoneNumberDtoOut createPhoneNumber(PhoneNumberDtoIn phoneNumberDtoIn) {
        PhoneNumber phoneNumber = modelMapper.map(phoneNumberDtoIn, PhoneNumber.class);
        PhoneNumber savedPhoneNumber = phoneNumberRepository.saveAndFlush(phoneNumber);
        return modelMapper.map(savedPhoneNumber, PhoneNumberDtoOut.class);
    }

    @Override
    public List<PhoneNumberDtoOut> getAllPhoneNumbers() {
        return phoneNumberRepository.findAll().stream()
                .map(phoneNumber -> modelMapper.map(phoneNumber, PhoneNumberDtoOut.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhoneNumberDtoOut> getPhoneNumberById(UUID id) {
        return phoneNumberRepository.findById(id)
                .map(phoneNumber -> modelMapper.map(phoneNumber, PhoneNumberDtoOut.class));
    }

    @Override
    public PhoneNumberDtoOut updatePhoneNumber(UUID id, PhoneNumberDtoIn phoneNumberDtoIn) {
        return phoneNumberRepository.findById(id).map(existingPhoneNumber -> {
            modelMapper.map(phoneNumberDtoIn, existingPhoneNumber);
            PhoneNumber updatedPhoneNumber = phoneNumberRepository.save(existingPhoneNumber);
            return modelMapper.map(updatedPhoneNumber, PhoneNumberDtoOut.class);
        }).orElseThrow(() -> new RuntimeException("Phone number not found with id " + id));
    }

    @Override
    public void deletePhoneNumber(UUID id) {
        if (phoneNumberRepository.existsById(id)) {
            phoneNumberRepository.deleteById(id);
        } else {
            throw new RuntimeException("Phone number not found with id " + id);
        }
    }
}
