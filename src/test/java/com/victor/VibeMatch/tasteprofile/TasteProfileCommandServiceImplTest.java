package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.exceptions.TasteProfileSaveException;
import com.victor.VibeMatch.tasteprofile.impl.TasteProfileCommandServiceImpl;
import com.victor.VibeMatch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasteProfileCommandServiceImplTest {

    @Mock
    private TasteProfileRepository tasteProfileRepository;

    @InjectMocks
    private TasteProfileCommandServiceImpl tasteProfileCommandService;

    private TasteProfile tasteProfile;

    private User user;

    @BeforeEach
    public void setUp(){
        user = User.builder().username("mock_name").build();
        tasteProfile = TasteProfile.builder().user(user).build();
    }

    @Test
    public void saveTasteProfile_shouldSuccessfullySaveTasteProfile(){
        //Act
        when(tasteProfileRepository.save(any(TasteProfile.class))).thenReturn(tasteProfile);

        TasteProfile savedProfile = tasteProfileCommandService.saveTasteProfile(tasteProfile);

        //Assert
        assertNotNull(savedProfile);
        assertEquals(tasteProfile, savedProfile);
        verify(tasteProfileRepository, times(1)).save(tasteProfile);

    }

    @Test
    public void saveTasteProfile_shouldThrowSaveException_onDataIntegrityException(){
        //Act
        when(tasteProfileRepository.save(tasteProfile)).thenThrow(DataIntegrityViolationException.class);

        //Assert
        var ex = assertThrows(TasteProfileSaveException.class, () -> {
            tasteProfileCommandService.saveTasteProfile(tasteProfile);
        });
        verify(tasteProfileRepository, times(1)).save(tasteProfile);
    }

    @Test
    public void saveTasteProfile_shouldThrowSaveException_onGenericException(){
        //Act
        when(tasteProfileRepository.save(tasteProfile)).thenThrow(DataIntegrityViolationException.class);

        //Assert
        var ex = assertThrows(Exception.class, () -> {
            tasteProfileCommandService.saveTasteProfile(tasteProfile);
        });
        verify(tasteProfileRepository, times(1)).save(tasteProfile);
    }

}