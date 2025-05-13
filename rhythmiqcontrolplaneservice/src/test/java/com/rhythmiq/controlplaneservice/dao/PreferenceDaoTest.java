package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;


import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PreferenceDaoTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    private DynamoDbTable<Preference> preferenceTable;

    private TableSchema<Preference> preferenceSchema;

    private PreferenceDao preferenceDao;

    @BeforeEach
    void setUp() {
        preferenceSchema = TableSchema.fromBean(Preference.class);
        when(dynamoDbClient.table(anyString(), eq(preferenceSchema))).thenReturn(preferenceTable);
        preferenceDao = new PreferenceDao(dynamoDbClient);
    }

    @Test
    void testCreatePreference() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        
        // Act
        preferenceDao.createPreference(preference);
        
        // Assert
        verify(preferenceTable).putItem(any(Preference.class));
        assertNotNull(preference.getCreatedAt());
        assertNotNull(preference.getUpdatedAt());
    }

    @Test
    void testGetPreference() {
        // Arrange
        String profileId = "profile1";
        String preferenceId = "pref1";
        Preference expectedPreference = createTestPreference(profileId, preferenceId, 0);
        Key key = Key.builder().partitionValue(profileId).sortValue(preferenceId).build();
        when(preferenceTable.getItem(eq(key))).thenReturn(expectedPreference);

        // Act
        Optional<Preference> result = preferenceDao.getPreference(profileId, preferenceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedPreference, result.get());
        verify(preferenceTable).getItem(eq(key));
    }

    @Test
    void testListPreferences() {
        // Arrange
        String profileId = "profile1";
        List<Preference> expectedPreferences = Arrays.asList(
            createTestPreference(profileId, "pref1", 0),
            createTestPreference(profileId, "pref2", 1)
        );
        @SuppressWarnings("unchecked")
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        @SuppressWarnings("unchecked")
        SdkIterable<Preference> sdkIterable = mock(SdkIterable.class);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(expectedPreferences.stream());
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(profileId).build());
        when(preferenceTable.query(eq(queryConditional))).thenReturn(pageIterable);

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
        verify(preferenceTable).putItem(any(Preference.class));
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
        verify(preferenceTable).deleteItem(any(Preference.class));
    }

    @Test
    void testSavePreference_ValidIndex() {
        // Arrange
        Preference preference = createTestPreference("profile1", "pref1", 0);
        @SuppressWarnings("unchecked")
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        @SuppressWarnings("unchecked")
        SdkIterable<Preference> sdkIterable = mock(SdkIterable.class);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Collections.<Preference>emptyList().stream());
        when(preferenceTable.scan((ScanEnhancedRequest) any())).thenReturn(pageIterable);

        // Act
        preferenceDao.savePreference(preference);

        // Assert
        verify(preferenceTable).putItem(any(Preference.class));
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
        @SuppressWarnings("unchecked")
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        @SuppressWarnings("unchecked")
        SdkIterable<Preference> sdkIterable = mock(SdkIterable.class);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(existingPreferences.stream());
        when(preferenceTable.scan((ScanEnhancedRequest) any())).thenReturn(pageIterable);

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
        @SuppressWarnings("unchecked")
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        @SuppressWarnings("unchecked")
        SdkIterable<Preference> sdkIterable = mock(SdkIterable.class);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(preferences.stream());
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(profileId).build());
        when(preferenceTable.query(eq(queryConditional))).thenReturn(pageIterable);

        List<String> newOrder = Arrays.asList("pref3", "pref1", "pref2");

        // Act
        preferenceDao.reorderPreferences(profileId, newOrder);

        // Assert
        verify(preferenceTable, times(3)).putItem(any(Preference.class));
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
        SdkIterable<Preference> sdkIterable = mock(SdkIterable.class);
        when(sdkIterable.stream()).thenReturn(preferences.stream());
        PageIterable<Preference> pageIterable = mock(PageIterable.class);
        when(pageIterable.items()).thenReturn(sdkIterable);
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(profileId).build());
        when(preferenceTable.query(eq(queryConditional))).thenReturn(pageIterable);

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