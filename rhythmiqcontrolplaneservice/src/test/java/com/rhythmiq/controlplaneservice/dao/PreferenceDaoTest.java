package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PreferenceDaoTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    private DynamoDbTable<Preference> preferenceTable;

    private PreferenceDao preferenceDao;

    @BeforeEach
    void setUp() {
        when(dynamoDbClient.table(anyString(), any())).thenReturn(preferenceTable);
        preferenceDao = new PreferenceDao(dynamoDbClient);
    }

    @Test
    void testCreatePreference() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        
        // Act
        preferenceDao.createPreference(preference);
        
        // Assert
        verify(preferenceTable).putItem(any());
        assertNotNull(preference.getCreatedAt());
        assertNotNull(preference.getUpdatedAt());
    }

    @Test
    void testGetPreference() {
        // Arrange
        String profileId = "profile1";
        String preferenceId = "pref1";
        Preference expectedPreference = createTestPreference(profileId, preferenceId, 0);
        when(preferenceTable.getItem(any())).thenReturn(expectedPreference);

        // Act
        Optional<Preference> result = preferenceDao.getPreference(profileId, preferenceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedPreference, result.get());
        verify(preferenceTable).getItem(any());
    }

    @Test
    void testListPreferences() {
        // Arrange
        String profileId = "profile1";
        List<Preference> expectedPreferences = Arrays.asList(
            createTestPreference(profileId, "pref1", 0),
            createTestPreference(profileId, "pref2", 1)
        );
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        Page<Preference> page = mock(Page.class);
        when(page.items()).thenReturn(expectedPreferences);
        when(pageIterable.iterator()).thenReturn(Collections.singletonList(page).iterator());
        when(preferenceTable.query(any(QueryConditional.class))).thenReturn(pageIterable);

        // Act
        List<Preference> result = preferenceDao.listPreferences(profileId);

        // Assert
        assertEquals(expectedPreferences, result);
        verify(preferenceTable).query(any(QueryConditional.class));
    }

    @Test
    void testUpdatePreference() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        Instant originalUpdatedAt = preference.getUpdatedAt();

        // Act
        preferenceDao.updatePreference(preference);

        // Assert
        verify(preferenceTable).putItem(any());
        assertTrue(preference.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testDeletePreference() {
        // Arrange
        String profileId = "profile1";
        String preferenceId = "pref1";

        // Act
        preferenceDao.deletePreference(profileId, preferenceId);

        // Assert
        verify(preferenceTable).deleteItem(any());
    }

    @Test
    void testSavePreference_ValidIndex() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        Page<Preference> page = mock(Page.class);
        when(page.items()).thenReturn(Collections.emptyList());
        when(pageIterable.iterator()).thenReturn(Collections.singletonList(page).iterator());
        when(preferenceTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

        // Act
        preferenceDao.savePreference(preference);

        // Assert
        verify(preferenceTable).putItem(any());
    }

    @Test
    void testSavePreference_InvalidIndex() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 100);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            preferenceDao.savePreference(preference);
        });
    }

    @Test
    void testSavePreference_MaxPreferencesReached() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        List<Preference> existingPreferences = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            existingPreferences.add(createTestPreference("profile1", "pref" + i, i));
        }
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        Page<Preference> page = mock(Page.class);
        when(page.items()).thenReturn(existingPreferences);
        when(pageIterable.iterator()).thenReturn(Collections.singletonList(page).iterator());
        when(preferenceTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            preferenceDao.savePreference(preference);
        });
    }

    @Test
    void testReorderPreferences() {
        // Arrange
        String profileId = "profile1";
        List<Preference> preferences = Arrays.asList(
            createTestPreference(profileId, "pref1", 0),
            createTestPreference(profileId, "pref2", 1),
            createTestPreference(profileId, "pref3", 2)
        );
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        Page<Preference> page = mock(Page.class);
        when(page.items()).thenReturn(preferences);
        when(pageIterable.iterator()).thenReturn(Collections.singletonList(page).iterator());
        when(preferenceTable.query(any(QueryConditional.class))).thenReturn(pageIterable);

        List<String> newOrder = Arrays.asList("pref3", "pref1", "pref2");

        // Act
        preferenceDao.reorderPreferences(profileId, newOrder);

        // Assert
        verify(preferenceTable, times(3)).putItem(any());
    }

    @Test
    void testReorderPreferences_TooManyPreferences() {
        // Arrange
        String profileId = "profile1";
        List<String> tooManyPreferences = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            tooManyPreferences.add("pref" + i);
        }

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            preferenceDao.reorderPreferences(profileId, tooManyPreferences);
        });
    }

    @Test
    void testReorderPreferences_InvalidPreferenceId() {
        // Arrange
        String profileId = "profile1";
        List<Preference> preferences = Arrays.asList(
            createTestPreference(profileId, "pref1", 0),
            createTestPreference(profileId, "pref2", 1)
        );
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        Page<Preference> page = mock(Page.class);
        when(page.items()).thenReturn(preferences);
        when(pageIterable.iterator()).thenReturn(Collections.singletonList(page).iterator());
        when(preferenceTable.query(any(QueryConditional.class))).thenReturn(pageIterable);

        List<String> invalidOrder = Arrays.asList("pref1", "invalidPref");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            preferenceDao.reorderPreferences(profileId, invalidOrder);
        });
    }

    private Preference createTestPreference(String profileId, String preferenceId, int index) {
        Preference preference = new Preference();
        preference.setProfileId(profileId);
        preference.setPreferenceId(preferenceId);
        preference.setType(Preference.PreferenceType.GENRE);
        preference.setValue("test-value");
        preference.setIndex(index);
        preference.setWeight(1.0);
        preference.setIsUserSet(true);
        preference.setCreatedAt(Instant.now());
        preference.setUpdatedAt(Instant.now());
        return preference;
    }
} 