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

actor Cache<K: Hashable, V: Sendable> {
    struct Entry {
        let value: V
        let expirationDate: Date?
    }

    private var cache: [K: Entry] = [:]
    private var expirationTask: Task<Void, Never>?
    private var expiredEntryHandler: (@Sendable (Entry) -> Void)?

    deinit {
        expirationTask?.cancel()
    }

    func setExpiredEntryHandler(_ expiredEntryHandler: @Sendable @escaping (Entry) -> Void) {
        self.expiredEntryHandler = expiredEntryHandler
    }

    func value(for key: K) -> V? {
        cache[key]?.value
    }

    func setValue(_ value: V, for key: K, expirationDate: Date? = nil) {
        cache[key] = Entry(value: value, expirationDate: expirationDate)
        if expirationTask == nil {
            startRemovingExpiredEntries()
        }
    }

    func removeValue(for key: K) {
        cache.removeValue(forKey: key)
    }

    func clear() -> [Entry] {
        let entriesRemoved = Array(cache.values)
        cache.removeAll()
        return entriesRemoved
    }

    private func startRemovingExpiredEntries() {
        expirationTask = Task { [weak self] in
            while !Task.isCancelled {
                await self?.removeExpiredEntries()
                try? await Task.sleep(for: .seconds(1.0))
            }
        }
    }

    private func removeExpiredEntries() {
        let now = Date()
        cache = cache.filter {
            let isExpired = $1.expirationDate.map { now > $0 } ?? false
            if isExpired {
                expiredEntryHandler?($1)
            }
            return !isExpired
        }
    }
}
