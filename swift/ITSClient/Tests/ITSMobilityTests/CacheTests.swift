/*
 * Software Name : ITSClient
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Software description: Swift ITS client.
 */

import Foundation
@testable import ITSMobility
import Testing

struct CacheTests {
    @Test("Cache setValue should save value")
    func cache_setValue_should_save_value() async {
        // Given
        let cache = Cache<String, String>()
        let key = "Key1"
        let value = "Value1"

        // When
        await cache.setValue(value, for: key)

        // Then
        #expect(await cache.value(for: key) == value)
    }

    @Test("Cache removeValue should remove value")
    func cache_removeValue_should_remove_value() async {
        // Given
        let cache = Cache<String, String>()
        let key1 = "Key1"
        let key2 = "Key2"
        let value2 = "Value2"
        await cache.setValue("Value1", for: key1)
        await cache.setValue(value2, for: key2)

        // When
        await cache.removeValue(for: key1)

        // Then
        #expect(await cache.value(for: key1) == nil)
        #expect(await cache.value(for: key2) == value2)
    }

    @Test("Cache clear should remove all values")
    func cache_clear_should_remove_all_values() async {
        // Given
        let cache = Cache<String, String>()
        let key1 = "Key1"
        let key2 = "Key2"
        await cache.setValue("Value1", for: key1)
        await cache.setValue("Value2", for: key2)

        // When
        await cache.clear()

        // Then
        #expect(await cache.value(for: key1) == nil)
        #expect(await cache.value(for: key2) == nil)
    }

    @Test("Cache should trigger an expired event if a validation date is set")
    func cache_should_trigger_an_expired_event_if_a_validation_date_is_set() async throws {
        // Given
        let cache = Cache<String, String>()
        let key = "Key1"

        // When
        await cache.setValue("Value1", for: key, expirationDate: Date().addingTimeInterval(2))

        try await confirmation(expectedCount: 1) { confirmation in
            await cache.setExpiredEntryHandler { _ in
                confirmation()
            }
            try await Task.sleep(for: .seconds(3))
        }

        // Then
        #expect(await cache.value(for: key) == nil)
    }
}
